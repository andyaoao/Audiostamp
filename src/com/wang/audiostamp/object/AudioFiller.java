package com.wang.audiostamp.object;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;

import com.wang.audiostamp.databases.SetDatabase;
import com.wang.audiostamp.fragment.PlayFragment;
import com.wang.audiostamp.service.BackRecordService;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;

public class AudioFiller {
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final String AUDIO_BACK_RECORD_FILE = "backrecord_temp.raw";
	private static final int RECORDER_SAMPLERATE = 44100;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_DEFAULT;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	private AudioRecord recorder = null;
	private int bufferSize = 0;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	private static AudioFiller thisPtr;
	private int ringBufferSize = 0;
	CircularObjectBuffer cBuffer;
	private boolean isBackRecord = false;

	public static AudioFiller getInstance() {
		if (thisPtr == null)
			thisPtr = new AudioFiller();
		return thisPtr;
	}

	public void setMemorySizeByTime(int timeInS) {
		ringBufferSize = RECORDER_SAMPLERATE * RECORDER_CHANNELS
				* RECORDER_AUDIO_ENCODING * timeInS;
	}

	private String getFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
	}
	
	public String getBackRecordPath() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}
		File tempFile = new File(filepath, AUDIO_BACK_RECORD_FILE);

		if (tempFile.exists())
			tempFile.delete();
		
		return (file.getAbsolutePath() + "/" + AUDIO_BACK_RECORD_FILE);
	}
	
	public String getTempFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath, AUDIO_RECORDER_FOLDER);

		if (!file.exists()) {
			file.mkdirs();
		}

		File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

		if (tempFile.exists())
			tempFile.delete();

		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}

	public void setIsBackRecord(boolean isBack) {
		isBackRecord = isBack;
	}

	public String startRecording() {
		bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferSize);

		int i = recorder.getState();
		if (i == 1)
			recorder.startRecording();

		isRecording = true;

		recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (isBackRecord)
					writeAudioByRing();
				else
					writeAudioDataToFile();
			}
		}, "AudioRecorder Thread");

		recordingThread.start();
		return getTempFilename();
	}

	private void writeAudioByRing() {
		byte data[] = new byte[bufferSize];
		cBuffer = new CircularObjectBuffer(ringBufferSize);
		cBuffer.clear();
		int read = 0;
		while (isRecording) {
			read = recorder.read(data, 0, bufferSize);
			if (AudioRecord.ERROR_INVALID_OPERATION != read) {
				try {
					//Log.i("BackRecordService", "TEST");
					cBuffer.write(data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void writeAudioDataToFile() {
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;

		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int read = 0;

		if (null != os) {
			while (isRecording) {
				read = recorder.read(data, 0, bufferSize);

				if (AudioRecord.ERROR_INVALID_OPERATION != read) {
					try {
						// Draw Table
						/*
						 * File file = new File(filename); int unit =
						 * ((RECORDER_SAMPLERATE
						 * )*AudioFormat.ENCODING_PCM_16BIT);
						 * Log.i("WRITE_TIME", (file.length()/unit)+" s");
						 */
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String saveBackFile(final String fileName) {
		if (null != recorder) {
			isRecording = false;

			int i = recorder.getState();
			if (i == 1)
				recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}
		String filepath = Environment.getExternalStorageDirectory().getPath();
		final File file = new File(filepath, AUDIO_RECORDER_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}
		Thread storeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				File tempFile = new File(fileName);
				if (tempFile.exists())
					tempFile.delete();

				OutputStream out = null;
				try {
					out = new FileOutputStream(fileName, true);
					byte[] buf = new byte[4096];
					int len;
					int realLeng = cBuffer.getWriteLength();
					while ((len = cBuffer.read(buf, 0, realLeng > 1024 ? 1024
							: realLeng)) > 0) {
						out.write(buf, 0, len);
						realLeng -= len;
					}
				} catch (Exception e) {
					Log.i("saveToBackTime", e.getMessage());
				} finally {
					try {
						out.close();
						cBuffer = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		isBackRecord = false;
		storeThread.start();
		//return getBackRecordPath();
		return fileName;
	}

	public String stopRecording() {
		if (null != recorder) {
			isRecording = false;

			int i = recorder.getState();
			if (i == 1)
				recorder.stop();
			recorder.release();

			recorder = null;
			recordingThread = null;
		}
		String outFileName = getFilename();
		
		copyWaveFile(getTempFilename(), outFileName);
		deleteTempFile();
		return outFileName;
	}

	private void deleteTempFile() {
		File file = new File(getTempFilename());		
		file.delete();
		file = new File(this.getBackRecordPath());
		if(file.exists())
			file.delete();
	}
	
	private double calcRecTimeBySize(long fileSize){
		double time = (double)fileSize/(RECORDER_SAMPLERATE * RECORDER_CHANNELS
				* RECORDER_AUDIO_ENCODING);
		return time;
	}
	
	private void copyWaveFile(String inFilename, String outFilename) {
		FileInputStream in = null;
		FileOutputStream out = null;
		FileInputStream backIn = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 1;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

		byte[] data = new byte[bufferSize];
		
		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			
			// If it has backrecord audio file
			File backFile = new File(this.getBackRecordPath());
			if(backFile.exists()){
				backIn = new FileInputStream(getBackRecordPath());
				long backFileLength = backFile.length();
				BackRecordService.backRecordTime = (int)calcRecTimeBySize(backFileLength)+1;
				totalAudioLen += backIn.getChannel().size();
			}
			backFile = null;
			totalAudioLen += in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			Log.i("AudioFiller", "File size: " + totalDataLen);

			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			if(backIn != null){
				while(backIn.read(data) != -1){
					out.write(data);
				}
			}
			while (in.read(data) != -1) {
				out.write(data);
			}
			if(backIn != null){
				backIn.close();
				
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {

		byte[] header = new byte[44];

		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8); // block align
		header[33] = 0;
		header[34] = RECORDER_BPP; // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}
}
