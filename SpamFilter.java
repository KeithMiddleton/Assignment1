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
				List<String> lines = Files.readAllLines(dir, Charset.defaultCharset());
				this.totals.put(currClass, dir.listFiles().length);
				for (String line : lines)
				{
					//Split to words and create / set values in ratings
					String[] words = line.split(" ");
					for (String word : words)
					{
						if (!this.ratings.containsKey(word))
						{
							//Add Word Key
							this.ratings.put(word, new HashMap<String, int>());
						}
						if (!this.ratings[word].containsKey(currClass))
						{
							//Add Class Key
							this.ratings.put(currClass, 0);
						}
						this.ratings[word][class]++;
					}
				}
			}
		}
		
		public ObservableList<Email> getEmails()
		{
			File[] fileList = new File(this.directory + "\\test").listFiles();
			for (File dir : fileList)
			{
				string currClass = dir.getName();
				for (File f : dir.listFiles())
				{
					List<String> lines = Files.readAllLines(f.getAbsolutePath(), Charset.defaultCharset());
					for (String line : lines)
					{
						String[] words = line.split(" ");
						for (String word : words)
						{
							
							
						}
					}
				}
			}
			ObservableList<Email> emails = FXCollections.observableArrayList();
			
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