package com.julioterra.moodyjulio.dataload.data;

public class Data_EmotionJournal extends Data_Journal {

	public long post_id; 
	public String emotion_intensity;

//	public EmotionJournalData(String timeStamp, String dateStamp,String emotion_L1, String emotion_L2, String emotion_L3,
//			String activity, String location, String people,String description, String post_id, String emotion_intensity) {
//		super(timeStamp, dateStamp, emotion_L1, emotion_L2, emotion_L3,
//				activity, location, people, description);
//		this.post_id = Long.parseLong(post_id);
//		this.emotion_intensity = emotion_intensity;
//	}

	public Data_EmotionJournal(String[] data_entry) {
		super(data_entry);
		this.post_id = Long.parseLong(data_entry[9]);
		this.emotion_intensity = data_entry[10];
	}

	public Data_EmotionJournal() {
		super();
		this.post_id = 0;
		this.emotion_intensity = "";
	}


	public long getPost_id() {
		return post_id;
	}

	public void setPost_id(long post_id) {
		this.post_id = post_id;
	}

	public String getEmotion_intensity() {
		return emotion_intensity;
	}

	public void setEmotion_intensity(String emotion_intensity) {
		this.emotion_intensity = emotion_intensity;
	}


	
}
