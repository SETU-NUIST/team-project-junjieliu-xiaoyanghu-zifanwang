package ljj.work11;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

class Movie
{
    private String title;
    private String director;
    private int year;
    private double rating;

// 用于JSON反序列化
    public Movie(String title, String director, int year, double rating) {
        this.title = title;
        this.director = director;
        this.year = year;
        this.rating = rating;
    }
    public Movie(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director)
    {
        this.director = director;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString()
    {
        return String.format("Title: %s | Director: %s | Year: %d | Rating: %.1f", title, director, year, rating);
    }
}

// Film Management
class MovieManager
{
    private static final String DATA_FILE = "movies.json";
    private static List<Movie> movies = new ArrayList<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void loadMoviesFromFile()
    {
        try (Reader reader = new FileReader(DATA_FILE)) {
            Type movieListType = new TypeToken
                    <List<Movie>>(){}.getType();
            List<Movie> loadedMovies = gson.fromJson(reader, movieListType);
            if (loadedMovies != null) {
                movies = loadedMovies;
            }
        }
        catch
        (FileNotFoundException e) {
            System.out.println("No data file found. Starting with empty list.");
        }
        catch
        (IOException e) {JOptionPane.showMessageDialog(null, "Error loading movies: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void saveMoviesToFile()
    {
        try (Writer writer = new FileWriter(DATA_FILE)) {gson.toJson(movies, writer);
        }
        catch
        (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving movies: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static List<Movie> getAllMovies() {
        return new ArrayList
                <>(movies);
    }

    public static void addMovie(Movie movie)
    {
        movies.add(movie);
        saveMoviesToFile();
    }

    public static boolean updateMovie(int index, Movie movie) {
        if (index >= 0 && index < movies.size()) {
            Movie existingMovie = movies.get(index);
            existingMovie.setTitle(movie.getTitle());
            existingMovie.setDirector(movie.getDirector());
            existingMovie.setYear(movie.getYear());
            existingMovie.setRating(movie.getRating());

            saveMoviesToFile();
            return true;
        }
        return false;
    }

    public static boolean deleteMovie(int index) {
        if (index >= 0 && index < movies.size()) {
            movies.remove(index);
            saveMoviesToFile();
            return true;
        }
        return false;
    }

    public static List<Movie> searchByTitle(String keyword) {
        return
                movies.stream().filter(movie -> movie.getTitle().toLowerCase().contains(keyword.toLowerCase())).collect(Collectors.toList());
    }

    public static List<Movie> searchByDirector(String keyword) {
        return
                movies.stream().filter(movie -> movie.getDirector().toLowerCase().contains(keyword.toLowerCase())).collect(Collectors.toList());
    }

    public static List<Movie> searchByYear(int year) {
        return
                movies.stream().filter(movie -> movie.getYear() == year).collect(Collectors.toList());
    }

    public static List<Movie> searchByYearRange(int startYear, int endYear) {
        return
                movies.stream().filter(movie -> movie.getYear() >= startYear && movie.getYear() <= endYear).collect(Collectors.toList());
    }

    public static List<Movie> searchByRatingRange(double minRating, double maxRating) {
        return
                movies.stream().filter(movie -> movie.getRating() >= minRating && movie.getRating() <= maxRating).collect(Collectors.toList());
    }

    public static List<Movie> searchMovies
            (String title, String director, Integer year, Double minRating, Double maxRating)
    {
        return
                movies.stream()
                        .filter(movie -> title.isEmpty() || movie.getTitle().toLowerCase().contains(title.toLowerCase()))
                        .filter(movie -> director.isEmpty() || movie.getDirector().toLowerCase().contains(director.toLowerCase()))
                        .filter(movie -> year == null || movie.getYear() == year)
                        .filter(movie -> minRating == null || movie.getRating() >= minRating)
                        .filter(movie -> maxRating == null || movie.getRating() <= maxRating)
                        .collect(Collectors.toList());
    }

    public static void sortMoviesByTitle()
    {
        movies.sort(Comparator.comparing(Movie::getTitle));
        saveMoviesToFile();
    }

    public static void sortMoviesByYear()
    {
        movies.sort(Comparator.comparingInt(Movie::getYear));
        saveMoviesToFile();
    }

    public static void sortMoviesByRating()
    {
        movies.sort(Comparator.comparingDouble(Movie::getRating).reversed());
        saveMoviesToFile();
    }

    public static int getMovieCount()
    {
        return
                movies.size();
    }
}

// Movie Management GUI Interface
class MovieManagerGUI extends JFrame {
    private final JTextArea displayArea;
    private final JTextField searchField;
    private final JComboBox<String> searchTypeCombo;

    public MovieManagerGUI() {
        setTitle("Movie Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLayout(new BorderLayout());

        // Initialize movie data
        MovieManager.loadMoviesFromFile();
        if (MovieManager.getAllMovies().isEmpty()) {
            addSampleMovies();
        }

        // Create top panel
        JPanel topPanel = new JPanel(new BorderLayout());

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout
                (FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));

        searchTypeCombo = new JComboBox<>(new String[]{"By Title", "By Director", "By Year", "By Year Range", "By Rating Range", "Advanced Search"});
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchTypeCombo);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton addButton = new JButton("Add Movie");
        JButton viewAllButton = new JButton("View All Movies");
        JButton editButton = new JButton("Edit Movie");
        JButton deleteButton = new JButton("Delete Movie");
        JButton sortButton = new JButton("Sort Movies");
        JButton statsButton = new JButton("Statistics");
        JButton exitButton = new JButton("Exit");

        buttonPanel.add(addButton);
        buttonPanel.add(viewAllButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(sortButton);
        buttonPanel.add(statsButton);
        buttonPanel.add(exitButton);

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Display area
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Movie List"));

        // Status bar
        JLabel statusLabel = new JLabel("Total Movies: " + MovieManager.getMovieCount());

        // Add to main window
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Event Handling
        addButton.addActionListener(_ -> addMovie());
        viewAllButton.addActionListener(_-> displayAllMovies());
        editButton.addActionListener(_-> editMovie());
        deleteButton.addActionListener(_-> deleteMovie());
        sortButton.addActionListener(_-> sortMovies());
        statsButton.addActionListener(_-> showStatistics());
        exitButton.addActionListener(_-> exitApplication());
        searchButton.addActionListener(_-> performSearch());

        // Initially display all movies
        displayAllMovies();
    }

    private void addSampleMovies() {
        MovieManager.addMovie(new Movie("The Shawshank Redemption", "Frank Darabont", 1994, 9.3));
        MovieManager.addMovie(new Movie("The Godfather", "Francis Ford Coppola", 1972, 9.2));
        MovieManager.addMovie(new Movie("The Dark Knight", "Christopher Nolan", 2008, 9.0));
        MovieManager.addMovie(new Movie("Pulp Fiction", "Quentin Tarantino", 1994, 8.9));
        MovieManager.addMovie(new Movie("Forrest Gump", "Robert Zemeckis", 1994, 8.8));
    }

    private void addMovie() {
        JTextField titleField = new JTextField();
        JTextField directorField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField ratingField = new JTextField();

        Object[] message = {"Title:", titleField, "Director:", directorField, "Year:", yearField, "Rating (0-10):", ratingField};

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Movie", JOptionPane.OK_CANCEL_OPTION);

        if
        (option == JOptionPane.OK_OPTION) {
            try {String title = titleField.getText().trim();
                String director =
                        directorField.getText().trim();
                int year =
                        Integer.parseInt(yearField.getText().trim());
                double rating =
                        Double.parseDouble(ratingField.getText().trim());

                if
                (title.isEmpty() || director.isEmpty()) {JOptionPane.showMessageDialog(this, "Title and director cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (year < 1900 || year > 2026) {
                    JOptionPane.showMessageDialog(this, "Year must be between 1900 and 2026!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (rating < 0 || rating > 10
                ) {
                    JOptionPane.showMessageDialog(this, "Rating must be between 0 and 10!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Movie movie = new Movie();
                movie.setTitle(title);
                movie.setDirector(director);
                movie.setYear(year);
                movie.setRating(rating);

                MovieManager.addMovie(movie);
                displayAllMovies();
                updateStatus();
                JOptionPane.showMessageDialog(this, "Movie added successfully!");
            }
            catch
            (NumberFormatException e) {JOptionPane.showMessageDialog(this, "Please enter valid numbers for year and rating!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editMovie() {
        List<Movie> movies = MovieManager.getAllMovies();
        if
        (movies.isEmpty()) {JOptionPane.showMessageDialog(this, "No movies to edit!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] movieTitles = movies.stream().map(m -> m.getTitle() + " (" + m.getYear() + ")").toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(this, "Select a movie to edit:", "Edit Movie", JOptionPane.QUESTION_MESSAGE, null, movieTitles, movieTitles[0]);

        if (selected != null) {
            int index = -1;
            for (int i = 0; i < movieTitles.length; i++) {
                if
                (movieTitles[i].equals(selected)) {index = i;
                    break;
                }
            }

            if (index >= 0) {
                Movie movie = movies.get(index);

                JTextField titleField = new JTextField(movie.getTitle());
                JTextField directorField = new JTextField(movie.getDirector());
                JTextField yearField = new JTextField(String.valueOf(movie.getYear()));
                JTextField ratingField = new JTextField(String.valueOf(movie.getRating()));

                Object[] message = {"Title:", titleField, "Director:", directorField, "Year:", yearField, "Rating (0-10):", ratingField};

                int option = JOptionPane.showConfirmDialog(this, message, "Edit Movie", JOptionPane.OK_CANCEL_OPTION);

                if
                (option == JOptionPane.OK_OPTION) {
                    try {
                        String title =
                                titleField.getText().trim();
                        String director =
                                directorField.getText().trim();
                        int year =
                                Integer.parseInt(yearField.getText().trim());
                        double rating =
                                Double.parseDouble(ratingField.getText().trim());

                        if
                        (title.isEmpty() || director.isEmpty()) {JOptionPane.showMessageDialog(
                                this, "Title and director cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        Movie updatedMovie = new Movie(title, director, year, rating);
                        boolean updateSuccess =
                                MovieManager.updateMovie(index, updatedMovie);
                        if
                        (updateSuccess) {
                            displayAllMovies();
                            updateStatus();
                            JOptionPane.showMessageDialog(
                                    this, "Movie updated successfully!");
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(
                                    this, "Failed to update movie!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    catch
                    (NumberFormatException e) {
                        JOptionPane.showMessageDialog(this, "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void deleteMovie() {
        List<Movie> movies = MovieManager.getAllMovies();
        if
        (movies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No movies to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] movieTitles = movies.stream().map(m -> m.getTitle() + " (" + m.getYear() + ")").toArray(String[]::new);

        String selected = (String) JOptionPane.showInputDialog(this, "Select a movie to delete:", "Delete Movie", JOptionPane.QUESTION_MESSAGE, null, movieTitles, movieTitles[0]);

        if (selected != null) {
            int index = -1;
            for (int i = 0; i < movieTitles.length; i++) {
                if
                (movieTitles[i].equals(selected)) {
                    index = i;
                    break;
                }
            }

            if (index >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + movies.get(index).getTitle() + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if
                (confirm == JOptionPane.YES_OPTION) {
                    boolean deleteSuccess =
                            MovieManager.deleteMovie(index);
                    if
                    (deleteSuccess) {
                        displayAllMovies();
                        updateStatus();
                        JOptionPane.showMessageDialog(this, "Movie deleted successfully!"
                        );
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(this, "Failed to delete movie!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void performSearch()
    {
        String searchType = (String) searchTypeCombo.getSelectedItem();
        String query = searchField.getText().trim();

        List<Movie> results = new ArrayList<>();
        if (searchType == null) {
            JOptionPane.showMessageDialog(this, "Please select the search type!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch
        (searchType) {
            case "By Title":
                results = MovieManager.searchByTitle(query);
                break;
            case "By Director":
                results = MovieManager.searchByDirector(query);
                break;
            case "By Year":
                try
                {int year = Integer.parseInt(query);
                    results = MovieManager.searchByYear(year);
                }
                catch
                (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid year!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;
            case "By Year Range":
                String[] years = query.split("-");
                if (years.length == 2) {
                    try
                    {int startYear = Integer.parseInt(years[0
                                ].trim());
                        int endYear = Integer.parseInt(years[1
                                ].trim());
                        results = MovieManager.searchByYearRange(startYear, endYear);
                    }
                    catch
                    (NumberFormatException e) {JOptionPane.showMessageDialog(this, "Please enter valid years (e.g., 2000-2010)", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Please enter range as 'start-end' (e.g., 2000-2010)", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;
            case "By Rating Range":
                String[] ratings = query.split("-"
                );
                if (ratings.length == 2
                ) {try
                    {double minRating = Double.parseDouble(ratings[0].trim());
                        double maxRating = Double.parseDouble(ratings[1].trim());
                        results = MovieManager.searchByRatingRange(minRating, maxRating);
                    }
                    catch
                    (NumberFormatException e) {JOptionPane.showMessageDialog(this, "Please enter valid ratings (e.g., 8.0-9.0)", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(this, "Please enter range as 'min-max' (e.g., 8.0-9.0)", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                break;
            case "Advanced Search":
                showAdvancedSearch();
                return;
        }

        displayMovies(results, "Search Results (" + results.size() + " found)");
    }

    private void showAdvancedSearch()
    {
        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));

        JTextField titleField = new JTextField();
        JTextField directorField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField minRatingField = new JTextField();
        JTextField maxRatingField = new JTextField();

        panel.add(new JLabel("Title contains:"));
        panel.add(titleField);
        panel.add(new JLabel("Director contains:"));
        panel.add(directorField);
        panel.add(new JLabel("Year (exact):"));
        panel.add(yearField);
        panel.add(new JLabel("Min Rating:"));
        panel.add(minRatingField);
        panel.add(new JLabel("Max Rating:"));
        panel.add(maxRatingField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Advanced Search",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if
        (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String director = directorField.getText().trim();
            Integer year = null;
            Double minRating = null;
            Double maxRating = null;

            try
            {if
                (!yearField.getText().trim().isEmpty()) {
                    year = Integer.parseInt(yearField.getText().trim());
                }
                if
                (!minRatingField.getText().trim().isEmpty()) {
                    minRating = Double.parseDouble(minRatingField.getText().trim());
                }
                if
                (!maxRatingField.getText().trim().isEmpty()) {
                    maxRating = Double.parseDouble(maxRatingField.getText().trim());
                }

                List<Movie> results = MovieManager.searchMovies(title, director, year, minRating, maxRating);
                displayMovies(results, "Advanced Search Results (" + results.size() + " found)");
            }
            catch
            (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sortMovies()
    {
        String[] options = {"By Title (A-Z)", "By Year (Oldest First)", "By Rating (Highest First)"
        };
        String choice = (String) JOptionPane.showInputDialog(this, "Sort movies by:", "Sort Movies", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice != null
        ) {switch
            (choice) {
                case "By Title (A-Z)":
                    MovieManager.sortMoviesByTitle();
                    break;
                case "By Year (Oldest First)":
                    MovieManager.sortMoviesByYear();
                    break;
                case "By Rating (Highest First)":
                    MovieManager.sortMoviesByRating();
                    break;
            }
            displayAllMovies();
            JOptionPane.showMessageDialog(this, "Movies sorted by " + choice.toLowerCase());
        }
    }

    private void showStatistics()
    {
        List<Movie> movies = MovieManager.getAllMovies();
        if
        (movies.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No movies in database!", "Statistics", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double avgRating = movies.stream().mapToDouble(Movie::getRating).average().orElse(0);
        int oldestYear = movies.stream().mapToInt(Movie::getYear).min().orElse(0);
        int newestYear = movies.stream().mapToInt(Movie::getYear).max().orElse(0);

        long highRated = movies.stream().filter(m -> m.getRating() >= 8.0).count();
        long mediumRated = movies.stream().filter(m -> m.getRating() >= 6.0 && m.getRating() < 8.0).count();
        long lowRated = movies.stream().filter(m -> m.getRating() < 6.0).count();

        String stats = String.format("""
                        === Movie Statistics ===
                        Total Movies: %d
                        Average Rating: %.2f/10
                        Year Range: %d - %d
                        High Rated (8.0+): %d
                        Medium Rated (6.0-7.9): %d
                        Low Rated (<6.0): %d""",
                        movies.size(), avgRating, oldestYear, newestYear, highRated, mediumRated, lowRated);

        JOptionPane.showMessageDialog(this, stats, "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void displayAllMovies()
    {
        List<Movie> movies = MovieManager.getAllMovies();
        displayMovies(movies, "All Movies (" + movies.size() + " total)");
    }

    private void displayMovies(List<Movie> movies, String title)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-3s %-40s %-20s %-6s %-5s\n", "#", "Title", "Director", "Year", "Rating"));
        sb.append( "-".repeat(80)).append("\n");

        for (int i = 0; i < movies.size(); i++) {
            Movie m = movies.get(i);
            sb.append(String.format("%-3d %-40s %-20s %-6d %-5.1f\n", i + 1,
                    m.getTitle().length() > 37 ? m.getTitle().substring(0, 37) + "..." : m.getTitle(),
                    m.getDirector().length() > 17 ? m.getDirector().substring(0, 17) + "..." : m.getDirector(),
                    m.getYear(), m.getRating()));
        }

        if
        (movies.isEmpty()) {
            sb.append("No movies found.\n");
        }

        displayArea.setText(sb.toString());
        ((TitledBorder) ((JScrollPane) getContentPane().getComponent(1)).getBorder()).setTitle(title);
        getContentPane().getComponent(1).revalidate();
    }

    private void updateStatus()
    {
        ((JLabel) getContentPane().getComponent(2)).setText("Total Movies: " + MovieManager.getMovieCount());
    }

    private void exitApplication()
    {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);

        if
        (confirm == JOptionPane.YES_OPTION) {MovieManager.saveMoviesToFile();
            System.exit(0);
        }
    }
}

// Main class
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        // GUI interface using JavaFX/Swing
        SwingUtilities.invokeLater(() -> {
            try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch
            (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to set system appearance", e);
            }

            MovieManagerGUI gui = new MovieManagerGUI();
            gui.setVisible(true);
        });
    }
}