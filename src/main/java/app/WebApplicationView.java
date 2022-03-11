package app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import parse.QtUiFileParser;

public class WebApplicationView {

	private static Logger LOGGER = Logger.getLogger(WebApplicationView.class.getName());
	private static FileHandler FILEHANDLER = null;

	public static void init(){
		try {
			FILEHANDLER = new FileHandler("logs.log", true);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		Logger l = Logger.getLogger("");
		FILEHANDLER.setFormatter(new SimpleFormatter());
		l.addHandler(FILEHANDLER);
		l.setLevel(Level.CONFIG);
	}
	
	public static void main(String[] args) {
		
		init();
		
		LOGGER.info("Started to work");
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
			LOGGER.info("Start ERROR : \n"+ e.getStackTrace());
		}

		try {
			LOGGER.info(" Arguments list : "+ Arrays.asList(args));
			boolean haveArgFile = false;
			boolean useBootstrapCSS = true;
			 
			List<String> dirFile = new ArrayList<String>();
			File curPwdExecDir = new File(".");
			String curPwdDir = curPwdExecDir.getAbsolutePath();
			
			
			for (String argString : args) {
				
				String[] arguments = argString.split("=");
				LOGGER.info(" Arguments split : "+ arguments.length);
				if( arguments!=null &&  arguments.length>1){
					LOGGER.info(" Arguments split : "+arguments[0].toString()+" ::  "+arguments[1].toString());

					if(arguments[0].toString().equalsIgnoreCase("-file")){
						LOGGER.info(" Arguments split set flag(haveArgFile) for non interactive mode ");
						haveArgFile = true;
						String tmpFilepath = curPwdDir+"/"+arguments[1];
						LOGGER.info(" Arguments split filename : "+ tmpFilepath);
						File tmpFile = new File(tmpFilepath);
						if(tmpFile != null && !tmpFile.isDirectory() && tmpFile.isFile() && tmpFile.getName().toLowerCase().endsWith(".ui")){
							LOGGER.info(" Arguments :: Add File : "+ tmpFile.getName());
							dirFile.add(arguments[1]);
						}else{
							LOGGER.info("ERROR :: Arguments split filename not found : "+ tmpFilepath);
						}
					}else if(arguments[0].toString().equalsIgnoreCase( "-bootstrapcss")){
						LOGGER.info(" >>>> "+arguments[0].toString()+" :: "+arguments[1].toString());
						useBootstrapCSS = ( arguments[1].toString().equalsIgnoreCase( "true")? true:false );
					} 
				} else {
					LOGGER.info(" Arguments NONE : "+ argString);
				}
				
			}
			if(!haveArgFile){
				LOGGER.info(" Search for QT/ui files  ");
				for (File tmpFile : curPwdExecDir.listFiles()) {
					LOGGER.info(" path  "+tmpFile.getName());
					if( !tmpFile.isDirectory() && tmpFile.isFile() && tmpFile.getName().toLowerCase().endsWith(".ui")){
						LOGGER.info(" Dir Arguments :: Add File : "+ tmpFile.getName());
						dirFile.add(tmpFile.getName());
					}else{
						LOGGER.info(" Skip ...  ");

					}
				}
				
			}
			for (String curFile : dirFile) {
				LOGGER.info(" -------------------  -------------------  ------------------- ");
				File selectedFile = new File(curFile);
				LOGGER.info(" >>>>> BEGIN - Arg File " + selectedFile.getName() + " is in progress");
				QtUiFileParser uiParser = new QtUiFileParser();
				uiParser.parse(selectedFile, false, useBootstrapCSS);
				LOGGER.info(" >>>>> END - Arg File " + selectedFile.getName() + " is in progress");
			}

			if(dirFile.size() == 0){

				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter uiFilter = new FileNameExtensionFilter("Ui Files", "ui");
				fileChooser.setFileFilter(uiFilter);
				File selectedFile = null;
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
					System.out.println(selectedFile.getName());
				}
				if(selectedFile != null){
					LOGGER.info("File " + selectedFile.getName() + " is chosen");
					QtUiFileParser uiParser = new QtUiFileParser();
					uiParser.parse(selectedFile, true, useBootstrapCSS);
				}
				else{
					LOGGER.info("File is not chosen");
				}
			}

		} catch (Exception e){

			LOGGER.info("FILE ERROR : \n"+ e.getStackTrace());
		}
		LOGGER.info("Finished work");
		
	}

}
