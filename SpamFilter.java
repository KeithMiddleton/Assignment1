import java.text.DecimalFormat
import java.util.List;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

public class SpamFilter extends ApplicationException
{
	public class Email
	{
		private String filename;
		private double spamProbability;
		private String actualClass;
		
		public Email(String filename, double spamProbability, String actualClass)
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
		private HashMap<String, HashMap<String, int>> ratings;
		
		public DataSource(String dir)
		{
			this.directory = dir;
			this.ratings = new HashMap<String, HashMap<String, int>>();
			this.totals = new HashMap<String, int>();
			//"word" : { "class1" : 0, "class2" : 0.5, "class3" : 0.5 }
		}
		
		public void train()
		{
			File[] fileList = new File(this.directory + "\\train").listFiles();
			for (File dir : fileList)
			{
				//Class
				string currClass = dir.getName();
				for (File f : dir.listFiles())
				{
					//Email
					List<String> covered = new ArrayList<String>();
					List<String> lines = Files.readAllLines(f.getAbsolutePath(), Charset.defaultCharset);
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
									this.ratings.put(word, new HashMap<String, int>());
								}
								if (!this.ratings[word].containsKey(currClass))
								{
									this.ratings[word].put(currClass, 0);
								}
								this.ratings[word][currClass]++;
								covered.add(word);
							}
						}
					}
					if (!this.totals.containsKey(currClass))
					{
						this.totals.put(currClass, 0);
					}
					this.totals[currClass]++;
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
				string currClass = dir.getName();
				for (File f : dir.listFiles())
				{
					//File
					List<String> lines = Files.readAllLines(f.getAbsolutePath(), Charset.defaultCharset());
					HashMap<String, double> hit = new HashMap<String, double>();
					for (String line : lines)
					{
						//Line
						String[] words = line.split(" ");
						for (String word : words)
						{
							//Word
							if (!hit.containsKey(word))
							{
								double hamProb = (this.ratings[word]["ham"] / this.totals["ham"]);
								double spamProb = (this.ratings[word]["spam"] / this.totals["spam"]);
								double fullProb = (spamProb / (spamProb + hamProb));
								hit.put(word, fullProb);
							}
						}
					}
					double hitSum = 0;
					items.forEach((k, v) -> 
					{
						hitSum += (Math.log(1 - v) - Math.log(v));
					});
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