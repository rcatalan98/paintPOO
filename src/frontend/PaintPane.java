package frontend;

import backend.CanvasState;
import backend.model.Figure;
import backend.model.Point;
import backend.model.Rectangle;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.stream.Collectors;

public class PaintPane extends BorderPane {

	// BackEnd.
	private final CanvasState canvasState;

	// Canvas y relacionados.
	private final Canvas canvas = new Canvas(800, 600);
	private final GraphicsContext gc = canvas.getGraphicsContext2D();

	// Botones Barra Izquierda.
	private final ToggleButton selectionButton = new ToggleButton("Seleccionar");
	private final ToggleButton deleteButton = new ToggleButton("Borrar");
	private final ToggleButton bringForwardButton = new ToggleButton("Al fondo");
	private final ToggleButton sendBackButton = new ToggleButton("Al frente");
	private final Slider strokeSlider = new Slider(1,50,0);
	private final ColorPicker strokeColorPicker = new ColorPicker(Color.BLACK);
	private final ColorPicker fillColorPicker = new ColorPicker(Color.YELLOW);

	// Dibujar una figura.
	private Point startPoint;

	// Seleccionar una figura.
	private final Collection<Figure> selectedFigures = new LinkedList<>();

	// StatusBar.
	private final StatusPane statusPane;

	public PaintPane(CanvasState canvasState, StatusPane statusPane) {
		this.canvasState = canvasState;
		this.statusPane = statusPane;
		List<ToggleButton> toolsList = new ArrayList<>();
		ToggleGroup tools = new ToggleGroup();
		toolsList.add(selectionButton);
		toolsList.addAll(Arrays.stream(FigureButtons.values()).map(FigureButtons::getButton).collect(Collectors.toList()));
		toolsList.add(deleteButton);
		toolsList.add(bringForwardButton);
		toolsList.add(sendBackButton);
		toolsList.forEach(tool -> { tool.setMinWidth(90); tool.setToggleGroup(tools); tool.setCursor(Cursor.HAND); });

		VBox buttonsBox = new VBox(10);
		buttonsBox.getChildren().addAll(toolsList);

		Label strokeText = new Label("Borde");
		strokeSlider.setShowTickMarks(true);
		strokeSlider.setShowTickLabels(true);
		buttonsBox.getChildren().add(strokeText);
		buttonsBox.getChildren().add(strokeSlider);
		buttonsBox.getChildren().add(strokeColorPicker);

		Label fillText = new Label("Relleno");
		buttonsBox.getChildren().add(fillText);
		buttonsBox.getChildren().add(fillColorPicker);

		buttonsBox.setPadding(new Insets(5));
		buttonsBox.setStyle("-fx-background-color: #999999");
		buttonsBox.setPrefWidth(100);
		gc.setLineWidth(1);

		canvas.setOnMousePressed(event -> startPoint = new Point(event.getX(), event.getY()));

		canvas.setOnMouseReleased(event -> {
			Point endPoint = new Point(event.getX(), event.getY());
			try {
				Figure newFigure = FigureButtons.fetchFigure(startPoint,endPoint);
				if (newFigure != null) {
					newFigure.setColorProperties(strokeColorPicker.getValue(),
							fillColorPicker.getValue(),
							strokeSlider.getValue());
					canvasState.addFigure(newFigure);
				}
			}catch (Exception e){
				statusPane.updateStatus(e.getMessage());
			}
			redrawCanvas();
		});

		canvas.setOnMouseMoved(event -> {
			Point eventPoint = new Point(event.getX(), event.getY());
			boolean found = false;
			StringBuilder label = new StringBuilder();
			for(Figure figure : canvasState.figures()) {
				if(figure.contains(eventPoint)) {
					found = true;
					label.append(figure.toString());
				}
			}
			if(found) {
				statusPane.updateStatus(label.toString());
			} else {
				statusPane.updateStatus(eventPoint.toString());
			}
		});

		canvas.setOnMouseClicked(event -> {
			if(selectionButton.isSelected()) {
				Point eventPoint = new Point(event.getX(), event.getY());
				selectedFigures.clear();
				if(!startPoint.equals(eventPoint)) {
					try {
						Rectangle container = new Rectangle(startPoint, eventPoint);
						for (Figure figure : canvasState.figures()) {
							if (figure.isInside(container)) {
								selectedFigures.add(figure);
							}
						}
					} catch(Exception e) {
						statusPane.updateStatus(e.getMessage());
					}
				}else {
					for (Figure figure : canvasState.figures()) {
						if(figure.contains(eventPoint)  ) {
							selectedFigures.clear();
							selectedFigures.add(figure);
						}
					}
				}
				StringBuilder label = new StringBuilder("Se seleccionó: ");

				if (!selectedFigures.isEmpty()) {
					selectedFigures.forEach(figure -> label.append(figure.toString()));
					statusPane.updateStatus(label.toString());
				} else {
					statusPane.updateStatus("Ninguna figura encontrada");
				}
				redrawCanvas();
			}
		});

		canvas.setOnMouseDragged(event -> {
			if(selectionButton.isSelected() && !selectedFigures.isEmpty()) {
				Point eventPoint = new Point(event.getX(), event.getY());
				double diffX = (eventPoint.getX() - startPoint.getX());
				double diffY = (eventPoint.getY() - startPoint.getY());
				startPoint = eventPoint;
				selectedFigures.forEach( figure -> figure.move(diffX,diffY));
				redrawCanvas();
			}
		});

		strokeColorPicker.setOnAction(event -> selectedFigures.forEach(figure -> figure.setStrokeColor(strokeColorPicker.getValue())));

		fillColorPicker.setOnAction(event -> {
			selectedFigures.forEach(figure -> figure.setFillColor(fillColorPicker.getValue()));
			redrawCanvas();
		});

		StrokeSliderHandler sliderHandler = new StrokeSliderHandler();

		strokeSlider.setOnMouseClicked(sliderHandler);

		strokeSlider.setOnMouseDragged(sliderHandler);

		deleteButton.setOnAction(event -> {
			canvasState.removeSelectedFigures(selectedFigures);
			selectedFigures.clear();
			deleteButton.setSelected(false);
			redrawCanvas();
		});

		bringForwardButton.setOnAction(event -> {
			canvasState.moveForward(selectedFigures);
			bringForwardButton.setSelected(false);
			redrawCanvas();
		});

		sendBackButton.setOnAction(event -> {
			canvasState.moveBackwards(selectedFigures);
			sendBackButton.setSelected(false);
			redrawCanvas();
		});

		setLeft(buttonsBox);
		setRight(canvas);
	}

	private void redrawCanvas() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		for(Figure figure : canvasState.figures()) {
			if(selectedFigures.contains(figure)) {
				gc.setStroke(Color.RED);
			} else {
				gc.setStroke(figure.getStrokeColor());
			}
			gc.setLineWidth(figure.getStrokeWidth());
			gc.setFill(figure.getFillColor());
			figure.draw(gc);
		}
	}

	private class StrokeSliderHandler implements EventHandler<MouseEvent>{
		@Override
		public void handle(MouseEvent event) {
			selectedFigures.forEach(figure -> figure.setStrokeWidth(strokeSlider.getValue()));
			redrawCanvas();
		}
	}

}
