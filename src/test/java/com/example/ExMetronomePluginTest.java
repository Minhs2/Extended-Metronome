package com.example;

import minhs2.exmetronome.ExMetronomePlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ExMetronomePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ExMetronomePlugin.class);
		RuneLite.main(args);
	}
}