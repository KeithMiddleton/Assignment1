import java.text.DecimalFormat;
import java.util.List;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.*;
import javafx.stage.DirectoryChooser;
import java.nio.charset.StandardCharsets;

public class SpamFilter extends Application
{
	public class Email
	{
		private String filename;
		private double spamProbability;
		private String actualClass;
		
		public Email(String filename, String actualClass, double spamProbability)
		{
			this.filename = filename;
			this.spamProbability = spamProbability;
			this.actualClass = actualClass;
		}
		
		public String getFilename() { return this.filename; }
		public double getSpamProbability() { return this.spamProbability; }
		public String getSpamProbRounded() 
		{
			DecimalFormat df = new DecimalFormat("0.00000");
			return df.format(this.spamProbability);
		}
		public String getActualClass() { return this.actualClass; }
		public void setFilename(String value) { this.filename = value; }
		public void setSpamProbability(double value) { this.spamProbability = value; }
		public void setActualClass(String value) { this.actualClass = value; }
	}
	
	public class DataSource
	{
		private String directory;
		private HashMap<String, HashMap<String, Integer>> ratings;
		private HashMap<String, Integer> totals;
		
		public DataSource(String dir)
		{
			this.directory = dir;
			this.ratings = new HashMap<String, HashMap<String, Integer>>();
			this.totals = new HashMap<String, Integer>();
			//"word" : { "class1" : 0, "class2" : 0.5, "class3" : 0.5 }
		}
		
		public void train()
		{
			File[] fileList = new File(this.directory + "\\train").listFiles();
			for (File dir : fileList)
			{
				//Class
				String currClass = dir.getName();
				for (File f : dir.listFiles())
				{
					//Email
					List<String> covered = new ArrayList<String>();
					Charset cs = StandardCharsets.US_ASCII;
					List<String> lines = null;
					System.out.println(f.getAbsolutePath());
					try {
  							lines = Files.readAllLines(f.toPath(), cs);
					}
					catch(Exception e) {
  							e.printStackTrace();
					}

					for (String line : lines)
					{
						//Line
						String[] words = line.split(" ");
						for (String word : words)
						{
							if (!covered.contains(word))
							{
								if (!this.ratings.containsKey(word))
								{
									this.ratings.put(word, new HashMap<String, Integer>());
								}
								if (!this.ratings.get(word).containsKey(currClass))
								{
									this.ratings.get(word).put(currClass, 0);
								}
								this.ratings.get(word).put(currClass, this.ratings.get(word).get(currClass) + 1);
								covered.add(word);
							}
						}
					}
					if (!this.totals.containsKey(currClass))
					{
						this.totals.put(currClass, 0);
					}
					this.totals.put(currClass, this.totals.get(currClass) + 1);
				}
			}
		}
		
		public ObservableList<Email> getEmails()
		{
			ObservableList<Email> emails = FXCollections.observableArrayList();
			File[] fileList = new File(this.directory + "\\test").listFiles();
			for (File dir : fileList)
			{
				//Class
				String currClass = dir.getName();
				for (File f : dir.listFiles())
				{
					//File
					List<String> lines = null;
					Charset cs = StandardCharsets.US_ASCII;
					try {
  							lines = Files.readAllLines(f.toPath(), cs);
					}
					catch(Exception e) {
  							e.printStackTrace();
					}
					
					HashMap<String, Double> hits = new HashMap<String, Double>();
					for (String line : lines)
					{
						//Line
						String[] words = line.split(" ");
						for (String word : words)
						{
							//Word
							if (!hits.containsKey(word))
							{
								double hamProb = (this.ratings.get(word).get("ham") / this.totals.get("ham"));
								double spamProb = (this.ratings.get(word).get("spam") / this.totals.get("spam"));
								double fullProb = (spamProb / (spamProb + hamProb));
								hits.put(word, fullProb);
							}
						}
					}
					double hitSum = 0;
					for(Map.Entry<String, Double> entry : hits.entrySet())
					{
						hitSum += (Math.log(1 - entry.getValue()) - Math.log(entry.getValue()));
					}
					double finalProb = 1 / 1 + Math.exp(hitSum);
					emails.add(new Email(f.getName(), currClass, 0));
				}
			}
			return emails;
		}
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		primaryStage.setTitle("Spam Filter");
		
		TableView view = new TableView();
		
		TableColumn<Email, String> column1 = new TableColumn<>("File");
		column1.setCellValueFactory(new PropertyValueFactory<>("filename"));
		
		TableColumn<Email, String> column2 = new TableColumn<>("Actual Class");
		column2.setCellValueFactory(new PropertyValueFactory<>("actualClass"));
		
		TableColumn<Email, String> column3 = new TableColumn<>("Spam Probability");
		column3.setCellValueFactory(new PropertyValueFactory<>("spamProbability"));
		
		view.getColumns().add(column1);
		view.getColumns().add(column2);
		view.getColumns().add(column3);
		
		VBox vbox = new VBox(view);
		Scene scene = new Scene(vbox);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		//Eventually we need to make buttons to reset the list and launch below code again
		
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File("."));
		File mainDirectory = directoryChooser.showDialog(primaryStage);
		
		DataSource source = new DataSource(mainDirectory.getAbsolutePath());
		source.train();
		for (Email e : source.getEmails())
		{
			view.getItems().add(e);
		}
		
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}
}