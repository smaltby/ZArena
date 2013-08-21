package com.github.zarena.utils;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Configuration extends FileConfiguration
{
	private final Map<String, String> comments = new HashMap<String, String>();
	private final DumperOptions yamlOptions = new DumperOptions();
	private final Representer yamlRepresenter = new YamlRepresenter();
	private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

	@Override
	public void loadFromString(String contents) throws InvalidConfigurationException
	{
		Validate.notNull(contents, "Contents cannot be null");

		//Handle keys and values
		Map<?, ?> input;
		try
		{
			input = (Map<?, ?>) yaml.load(contents);
		} catch (YAMLException e)
		{
			throw new InvalidConfigurationException(e);
		} catch (ClassCastException e)
		{
			throw new InvalidConfigurationException("Top level is not a Map.");
		}

		if (input != null)
			convertMapsToSections(input, this);

		//Handle comments and header
		boolean headerDone = false;
		String currentComment = "";
		for(String line : contents.split("\n"))
		{
			if(line.startsWith("#"))
				currentComment += line + "\n";
			else if(line.matches("\\S.+:.*"))
			{
				//If the first key has passed and no header has been registered, then there is no header
				headerDone = true;
				if(currentComment.length() <= 0)
					continue;
				//Substring to remove trailing new line
				setComment(line.substring(0, line.indexOf(':')), currentComment.substring(0, currentComment.length() - 1));
				//All comments begin with a new line to space out between keys, increasing readability
				currentComment = "\n";
			} else
			{
				if(!headerDone)
				{
					options().header(currentComment);
					headerDone = true;
				}
				//All comments begin with a new line to space out between keys, increasing readability
				currentComment = "\n";
			}
		}
	}

	protected void convertMapsToSections(Map<?, ?> input, ConfigurationSection section)
	{
		for (Map.Entry<?, ?> entry : input.entrySet())
		{
			String key = entry.getKey().toString();
			Object value = entry.getValue();

			if (value instanceof Map)
				convertMapsToSections((Map<?, ?>) value, section.createSection(key));
			else
				section.set(key, value);
		}
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

	@Override
	public String saveToString()
	{
		yamlOptions.setIndent(4);
		yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		String dump = "";
		if (options().header() != null)
		{
			dump += options().header();
			dump += "\r\n";
		}
		if (comments.isEmpty())
		{
			// No top-level comments, just dump everything at once
			dump += yaml.dump(getValues(false));
		} else
		{
			for (Map.Entry<String, Object> me : getValues(false).entrySet())
			{
				String comment = comments.get(me.getKey());
				if (comment != null)
				{
					dump += comment;
					dump += "\r\n";
				}

				dump += yaml.dump(Collections.singletonMap(me.getKey(), me.getValue()));
			}
		}
		return dump;
	}

	public String buildHeader()
	{
		return "";
	}

	public static Configuration loadConfiguration(File file)
	{
		Validate.notNull(file, "File cannot be null");

		Configuration config = new Configuration();

		try
		{
			config.load(file);
		} catch (IOException ex)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file, ex);
		} catch (InvalidConfigurationException ex)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + file , ex);
		}

		return config;
	}

	public static Configuration loadConfiguration(InputStream stream)
	{
		Validate.notNull(stream, "Stream cannot be null");

		Configuration config = new Configuration();

		try
		{
			config.load(stream);
		} catch (IOException ex)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
		} catch (InvalidConfigurationException ex)
		{
			Bukkit.getLogger().log(Level.SEVERE, "Cannot load configuration from stream", ex);
		}

		return config;
	}
}
