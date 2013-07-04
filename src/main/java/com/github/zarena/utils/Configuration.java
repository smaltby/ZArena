package com.github.zarena.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.*;

public class Configuration extends ConfigurationNode
{
	private Yaml yaml;
	private File file;
	private String header = null;
	private final Map<String, String> comments = new HashMap<String, String>();

	public Configuration(File file)
	{
		super("", new HashMap<String, Object>());

		DumperOptions options = new DumperOptions();

		options.setIndent(4);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		yaml = new Yaml(new SafeConstructor(), new EmptyNullRepresenter(), options);

		this.file = file;

		load();
	}

	/**
	 * Loads the configuration file. All errors are thrown away.
	 */
	private void load()
	{
		FileInputStream stream = null;

		try
		{
			stream = new FileInputStream(file);
			read(yaml.load(new UnicodeReader(stream)));
			//Load the comments and header. Done through a BufferedReader instead of through the actual YAML library,
			//because it seems easier that way. (YAML has no native support for programmatically adding/reading
			//comments, afaik)
			BufferedReader reader = new BufferedReader(new FileReader(file));
			boolean headerDone = false;
			String currentComment = "";
			String line;
			while ((line = reader.readLine()) != null)
			{
				if(line.startsWith("#"))
					currentComment += line + "\n";
				else if(line.matches("\\S.+:.*"))
				{
					//If the first key has passed and no header has been registered, then there is no header
					headerDone = true;
					if(currentComment.length() == 0)
						continue;
					//Substring to remove trailing new line
					setComment(line.substring(0, line.indexOf(':')), currentComment.substring(0, currentComment.length() - 1));
					//All comments begin with a new line to space out between keys, increasing readability
					currentComment = "\n";
				} else
				{
					if(!headerDone)
					{
						setHeader(currentComment);
						headerDone = true;
					}
					//All comments begin with a new line to space out between keys, increasing readability
					currentComment = "\n";
				}
			}
			reader.close();
		} catch (IOException e)
		{
			root = new HashMap<String, Object>();
		} catch (ConfigurationException e)
		{
			root = new HashMap<String, Object>();
		} finally
		{
			try
			{
				if (stream != null)
					stream.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Set the header for the file as a series of lines that are terminated
	 * by a new line sequence.
	 *
	 * @param headerLines header lines to prepend
	 */
	public void setHeader(String... headerLines)
	{
		StringBuilder header = new StringBuilder();

		for (String line : headerLines)
		{
			if (header.length() > 0)
				header.append("\r\n");
			header.append(line);
		}

		setHeader(header.toString());
	}

	/**
	 * Set the header for the file. A header can be provided to prepend the
	 * YAML data output on configuration save. The header is
	 * printed raw and so must be manually commented if used. A new line will
	 * be appended after the header, however, if a header is provided.
	 *
	 * @param header header to prepend
	 */
	public void setHeader(String header)
	{
		this.header = header;
	}

	/**
	 * Return the set header.
	 *
	 * @return The header comment.
	 */
	public String getHeader()
	{
		return header;
	}

	/**
	 * Set the top-level comments.
	 *
	 * @param comments The top-level comments, mapped by property path
	 */
	public void setComments(Map<String, String> comments)
	{
		comments.clear();
		for (Map.Entry<String, String> me : comments.entrySet())
		{
			// Do this so each key is checked
			setComment(me.getKey(), me.getValue());
		}
	}

	/**
	 * Set the comment for the given property path.
	 *
	 * @param path    The top-level property path
	 * @param comment The property comment
	 */
	public void setComment(String path, String comment)
	{
		if (path.contains("."))
			throw new IllegalArgumentException("path must be a top-level path: " + path);
		comments.put(path, comment);
	}

	/**
	 * Return the top-level comments.
	 *
	 * @return The top-level comments, mapped by property path
	 */
	public Map<String, String> getComments()
	{
		return Collections.unmodifiableMap(comments);
	}

	/**
	 * Saves the configuration to disk. All errors are clobbered.
	 *
	 * @return true if it was successful
	 */
	public boolean save()
	{
		FileOutputStream stream = null;

		File parent = file.getParentFile();

		if (parent != null)
			parent.mkdirs();

		try
		{
			stream = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			if (header != null)
			{
				writer.append(header);
				writer.append("\r\n");
			}

			if (comments.isEmpty())
			{
				// No top-level comments, just dump everything at once
				yaml.dump(root, writer);
			} else
			{
				for (Map.Entry<String, Object> me : root.entrySet())
				{
					String comment = comments.get(me.getKey());
					if (comment != null)
					{
						writer.append(comment);
						writer.append("\r\n");
					}

					yaml.dump(Collections.singletonMap(me.getKey(), me.getValue()), writer);
				}
			}
			return true;
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				if (stream != null)
					stream.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	@SuppressWarnings("unchecked")
	private void read(Object input) throws ConfigurationException
	{
		try
		{
			if (null == input)
			{
				root = new HashMap<String, Object>();
			} else
			{
				root = (Map<String, Object>) input;
			}
		} catch (ClassCastException e)
		{
			throw new ConfigurationException("Root document must be an key-value structure");
		}
	}

	/**
	 * This method returns an empty ConfigurationNode for using as a
	 * default in methods that select a node from a node list.
	 *
	 * @return The empty node.
	 */
	public static ConfigurationNode getEmptyNode()
	{
		return new ConfigurationNode("", new HashMap<String, Object>());
	}

	class EmptyNullRepresenter extends Representer
	{

		public EmptyNullRepresenter()
		{
			super();
			this.nullRepresenter = new EmptyRepresentNull();
		}

		protected class EmptyRepresentNull implements Represent
		{
			public Node representData(Object data)
			{
				return representScalar(Tag.NULL, ""); // Changed "null" to "" so as to avoid writing nulls
			}
		}

		// Code borrowed from snakeyaml (http://code.google.com/p/snakeyaml/source/browse/src/test/java/org/yaml/snakeyaml/issues/issue60/SkipBeanTest.java)
		@Override
		protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag)
		{
			NodeTuple tuple = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
			Node valueNode = tuple.getValueNode();
			if (valueNode instanceof CollectionNode)
			{
				// Removed null check
				if (Tag.SEQ.equals(valueNode.getTag()))
				{
					SequenceNode seq = (SequenceNode) valueNode;
					if (seq.getValue().isEmpty())
					{
						return null; // skip empty lists
					}
				}
				if (Tag.MAP.equals(valueNode.getTag()))
				{
					MappingNode seq = (MappingNode) valueNode;
					if (seq.getValue().isEmpty())
					{
						return null; // skip empty maps
					}
				}
			}
			return tuple;
		}
		// End of borrowed code
	}
}
