package com.eclipse.OPMW;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StudentRecordHandler extends DefaultHandler {
	boolean bFirstName = false;
	boolean bLastName = false;
	boolean bNickName = false;
	boolean bMarks = false;
	public int valueLength=0;
	
	
	/*public static String xmlstring = "<?xml version=\"1.0\"?><class><student rollno=\"393\"><firstname>dinkar</firstname><lastname>kadkadkad</lastname> <nickname>dinkar</nickname><marks>85</marks></student>" +
    "<student rollno=\"493\"><firstname>Vaneet</firstname><lastname>Gupta</lastname><nickname>vinni</nickname><marks>95</marks></student><student rollno=\"593\"><firstname>jasvirjazz</firstname>" +
    "<lastname>singn</lastname><nickname>jazz</nickname><marks>90</marks></student><student rollno=\"5931\"><firstname>jasvir1</firstname><lastname>singn1</lastname><nickname>jazz1</nickname>" +
    "<marks>901</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\">" +
    "<firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname>" +
    "<nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student>" +
    "<student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
    "<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
    "<marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\">" +
    "<firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname>" +
    "<nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student>" +
    "<student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
    "<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
    "<marks>902</marks></student></class>";*/
	
	public static String xmlstring = "<?xml version=\"1.0\"?><class><student rollno=\"393\"><firstname>dinkar</firstname><lastname>kadkadkad</lastname> <nickname>dinkar</nickname><marks>85</marks></student>" +
	"<student rollno=\"493\"><firstname>Vaneet</firstname><lastname>Gupta</lastname><nickname>vinni</nickname><marks>95</marks></student><student rollno=\"593\"><firstname>jasvirjazz</firstname>" +
    "<lastname>singn</lastname><nickname>jazz</nickname><marks>90</marks></student><student rollno=\"5931\"><firstname>jasvir1</firstname><lastname>singn1</lastname><nickname>jazz1</nickname>" +
    "<marks>901</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\">" +
	"<firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname>" +
	"<nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student>" +
    "<student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
    "<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
    "<marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\">" +
	"<firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname>" +
	"<nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student>" +
	"<student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student>" + "<student rollno=\"393\"><firstname>dinkar</firstname><lastname>kadkadkad</lastname> <nickname>dinkar</nickname><marks>85</marks></student>" +
	"<student rollno=\"493\"><firstname>Vaneet</firstname><lastname>Gupta</lastname><nickname>vinni</nickname><marks>95</marks></student><student rollno=\"593\"><firstname>jasvirjazz</firstname>" +
	"<lastname>singn</lastname><nickname>jazz</nickname><marks>90</marks></student><student rollno=\"5931\"><firstname>jasvir1</firstname><lastname>singn1</lastname><nickname>jazz1</nickname>" +
	"<marks>901</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\">" +
	"<firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname>" +
	"<nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student>" +
	"<student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\">" +
	"<firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname>" +
	"<nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student>" +
	"<student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student>" + "<student rollno=\"393\"><firstname>dinkar</firstname><lastname>kadkadkad</lastname> <nickname>dinkar</nickname><marks>85</marks></student>" +
	"<student rollno=\"493\"><firstname>Vaneet</firstname><lastname>Gupta</lastname><nickname>vinni</nickname><marks>95</marks></student><student rollno=\"593\"><firstname>jasvirjazz</firstname>" +
	"<lastname>singn</lastname><nickname>jazz</nickname><marks>90</marks></student><student rollno=\"5931\"><firstname>jasvir1</firstname><lastname>singn1</lastname><nickname>jazz1</nickname>" +
	"<marks>901</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\">" +
	"<firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname>" +
	"<nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student>" +
	"<student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\">" +
	"<firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname>" +
	"<nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student>" +
	"<student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student>" + "<student rollno=\"393\"><firstname>dinkar</firstname><lastname>kadkadkad</lastname> <nickname>dinkar</nickname><marks>85</marks></student>" +
	"<student rollno=\"493\"><firstname>Vaneet</firstname><lastname>Gupta</lastname><nickname>vinni</nickname><marks>95</marks></student><student rollno=\"593\"><firstname>jasvirjazz</firstname>" +
	"<lastname>singn</lastname><nickname>jazz</nickname><marks>90</marks></student><student rollno=\"5931\"><firstname>jasvir1</firstname><lastname>singn1</lastname><nickname>jazz1</nickname>" +
	"<marks>901</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\">" +
	"<firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname>" +
	"<nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student>" +
	"<student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\">" +
	"<firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname>" +
	"<nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname><marks>902</marks></student>" +
	"<student rollno=\"5933\"><firstname>jasvir3</firstname><lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5933\"><firstname>jasvir3</firstname>" +
	"<lastname>singn3</lastname><nickname>jazz3</nickname><marks>903</marks></student><student rollno=\"5932\"><firstname>jasvir2</firstname><lastname>singn2</lastname><nickname>jazz2</nickname>" +
	"<marks>902</marks></student></class>";
	

	@Override
	public void startElement(String uri,
							 String localName, String qName, Attributes attributes)
			throws SAXException {
		if (qName.equalsIgnoreCase("student")) {
			String rollNo = attributes.getValue("rollno");
			valueLength+=rollNo.length();
		} else if (qName.equalsIgnoreCase("firstname")) {
			bFirstName = true;
			valueLength+=5;
		} else if (qName.equalsIgnoreCase("lastname")) {
			bLastName = true;
			valueLength+=6;
		} else if (qName.equalsIgnoreCase("nickname")) {
			bNickName = true;
			valueLength+=7;
		}
		else if (qName.equalsIgnoreCase("marks")) {
			bMarks = true;
			valueLength+=8;
		}
	}


	public void endElement(String uri,
						   String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("student")) {
			valueLength+=qName.length();
		}
	}
	@Override
	public void characters(char ch[],
						   int start, int length) throws SAXException {
		if (bFirstName) {
			valueLength += length+1;
			bFirstName = false;
		} else if (bLastName) {
			valueLength += length+2;
			bLastName = false;
		} else if (bNickName) {
			valueLength += length+3;
			bNickName = false;
		} else if (bMarks) {
			valueLength += length+4;
			bMarks = false;
		}
	}

}