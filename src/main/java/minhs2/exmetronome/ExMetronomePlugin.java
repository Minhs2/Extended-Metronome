/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package minhs2.exmetronome;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.net.URL;

import java.io.IOException;
import java.io.File;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import src.main.java.com.adonax.audiocue.AudioCue;

@PluginDescriptor(
	name = "Extended Metronome",
	description = "Play a sound on a specified tick to aid in efficient skilling",
	tags = {"skilling", "tick", "timers"},
	enabledByDefault = false
)
public class ExMetronomePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ExMetronomePluginConfiguration config;

	private int tickCounter = 0;
	private boolean shouldTock = false;

	private AudioCue soundPlayer;
	private AudioCue musicPlayer;
	private int milliSecPosition;

	@Provides
	ExMetronomePluginConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExMetronomePluginConfiguration.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		URL url = new File("bruh.wav").toURI().toURL();
		soundPlayer = AudioCue.makeStereoCue(url, 2);
		soundPlayer.open();

		url = new File("metro.wav").toURI().toURL();
		musicPlayer = AudioCue.makeStereoCue(url, 1);
		musicPlayer.open();
		musicPlayer.play(0);
		musicPlayer.stop(0);
		milliSecPosition = 0;

		//REMEMBER TO REMOVE THIS
		System.out.println("soundPlayer opened!");
	}

	protected void shutDown()
	{
		tickCounter = 0;
		shouldTock = false;
		soundPlayer.close();
		musicPlayer.close();

		//REMEMBER TO REMOVE THIS
		System.out.println("soundPlayer closed!");
	}

	@Subscribe
	public void onGameTick(GameTick tick) {


		if (config.tickCount() == 0) {
			return;
		}

		if (++tickCounter % config.tickCount() == 0) {
			// As playSoundEffect only uses the volume argument when the in-game volume isn't muted, sound effect volume
			// needs to be set to the value desired for ticks or tocks and afterwards reset to the previous value.
			int previousVolume = client.getSoundEffectVolume();

			if (shouldTock && config.tockVolume() > 0) {
				client.setSoundEffectVolume(config.tockVolume());
				client.playSoundEffect(SoundEffectID.GE_DECREMENT_PLOP, config.tockVolume());


			} else if (config.tickVolume() > 0) {
				client.setSoundEffectVolume(config.tickVolume());
				client.playSoundEffect(SoundEffectID.GE_INCREMENT_PLOP, config.tickVolume());
			}

			client.setSoundEffectVolume(previousVolume);

			shouldTock = !shouldTock;
		}

		if(config.customSound()){
			soundPlayer.play((float)config.customVolume()/127);
		}

		if(config.customMusic()){
			if(musicPlayer.getIsActive(0)==true) {
				musicPlayer.stop(0);
			}
			musicPlayer.setVolume(0,(float)config.musicVolume()/127);
			musicPlayer.setMillisecondPosition(0, milliSecPosition);
			musicPlayer.start(0);
			musicPlayer.setSpeed(0,1);
			milliSecPosition = milliSecPosition + 600;


		} else {
			if(musicPlayer.getIsActive(0)==true) {
				musicPlayer.stop(0);
				milliSecPosition = 0;
			}
		}
	}
}
