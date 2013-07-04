package com.github.zarena.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author Joshua
 */
public class ECommand
{
    private ArrayList<String> args, flags;


    /**
     * Converts elements available in onCommand into an MCTCommand
     * @param label the first word after the slash
     * @param args all following words and args
     */
    public ECommand(String label, String[] args)
    {
        this.args = new ArrayList<String>();
        this.flags = new ArrayList<String>();

        this.args.add(label);

        for(String s : args) {
            if(s.startsWith("-"))
                this.flags.add(s.replace("-", ""));
            else
                this.args.add(s);
        }
    }

    /**
     * Parses the given command to make an MCTCommand that represents it
     * @param slashCommand command
     */
    public ECommand(String slashCommand)
    {
        this.args = new ArrayList<String>();
        this.flags = new ArrayList<String>();

        slashCommand = slashCommand.substring(1); //take off the leading /

        Scanner scan = new Scanner(slashCommand);

        String s;
        while(scan.hasNext()) {
            s = scan.next();
            if(s.startsWith("-"))
                flags.add(s.replace("-", ""));
            else
                args.add(s);
        }
        scan.close();
    }

    /**
     * Copies the passed args and flags into a new MCTCommand
     * @param args args
     * @param flags flags
     */
    public ECommand(String[] args, String[] flags)
    {
        this.args = new ArrayList<String>();
        this.flags = new ArrayList<String>();

        this.args.addAll(Arrays.asList(args));
        for(int i = 0; i < flags.length; i++)
        {
        	flags[i] = flags[i].replace("-", "");
        }
        this.flags.addAll(Arrays.asList(flags));
    }

    /**
     * Checks whether or not the flag is present in the command.
     * @param flag flag
     * @return has flag
     */
    public boolean hasFlag(String flag)
    {
        return flags.contains(flag);
    }

    @Override
    public String toString()
    {
        String nu = "/";
        for(String s : args) {
            nu += s;
            nu += " ";
        }

        nu += "(";
        for(String s : flags) {
            nu += s;
            nu += ", ";
        }
        nu += ")";

        return nu;
    }

    /**
     * Gets the argument at the specified index.
     * Note: Use when you don't want to have ArgumentCountException thrown.
     * @param i	index
     * @return	argument at index, or an empty String if there is none
     */
    public String getArgAtIndex(final int i)
    {
    	if(i >= args.size())
            return "";
        return args.get(i);
    }

    public boolean hasArgAtIndex(int i)
    {
        return args.size() > i;
    }

    /**
     * Get the argument at the specified index
     * @param i	index
     * @return	argument at index
     * @throws ArgumentCountException
     */
    public String get(int i) throws ArgumentCountException
    {
    	if(i >= args.size())
            throw new ArgumentCountException(i);
    	
        return getArgAtIndex(i);
    }

    /**
     * Gets all flags in the command. IE '-flag', '-uo', '-q'.
     * @return array of flags, sans the '-'
     */
    public String[] getFlags()
    {
        return flags.toArray(new String[flags.size()]);
    }

    /**
     * Concatenates and returns all the non-flag arguments with indices in the range [index, END) where END is the index of the last argument.
     * @param index index to begin concatenation at
     * @return the constructed String
     */
    public String concatAfter(int index)
    {
        if(index == args.size()-1)
            return args.get(index);

        return args.get(index) + " " + concatAfter(index+1);
    }


}
