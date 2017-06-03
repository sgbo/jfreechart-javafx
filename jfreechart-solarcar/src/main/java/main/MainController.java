package main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.interaction.MouseHandlerFX;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

//TODO Farben
//TODO Mehrere Axen
//TODO SlidingXYDataSet
//TODO - Zoom nur jeden x-ten Wert anzeigen
//TODO - Scroll

public class MainController implements Initializable {
    static final String fontName = "Palatino";

    private boolean run = true;

    @FXML
    BorderPane rootPane;

    //    @FXML
//    private ScrollBar chartScrollBar;
//
//    @FXML
//    private Slider zoomSlider;
//
//    @FXML
//    private Button liveButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        final TimeSeriesCollection ds1 = createDataset();
        final TimeSeriesCollection ds2 = createDataset2();

        TimeSeries s1 = (TimeSeries) ds1.getSeries().get(0);
        TimeSeries s2 = (TimeSeries) ds2.getSeries().get(0);
        s1.setMaximumItemAge(10000);
        s2.setMaximumItemAge(10000);

        JFreeChart chart = createChart(ds1, ds2);

        ChartViewer viewer = new ChartViewer(chart);
        viewer.setOnContextMenuRequested(null);

        viewer.getCanvas().addChartMouseListener(new ChartMouseListenerFX() {
            @Override
            public void chartMouseClicked(ChartMouseEventFX event) {
                if (event.getTrigger().getButton() == MouseButton.SECONDARY) {
                    AxisEntity axisEntity = (AxisEntity) event.getEntity();
                    System.out.println(axisEntity.getAxis().getLabel());
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEventFX event) {

            }
        });

        // we need to set the size, to auto-resize the CartViewer when the rootPane is resized
        rootPane.setPrefSize(viewer.getPrefWidth(), viewer.getPrefHeight());
        rootPane.setCenter(viewer);

        Task task = new Task() {
            private int counter = 1;

            @Override
            protected Void call() throws Exception {
                while (run) {
                    Platform.runLater(new Runnable() {
                        TimeSeries s1 = (TimeSeries) ds1.getSeries().get(0);
                        TimeSeries s2 = (TimeSeries) ds2.getSeries().get(0);

                        @Override
                        public void run() {
                            boolean notify = counter % 25 == 0;

                            Date date = new Date();
                            Millisecond millisecond = new Millisecond(date);

//                            Date lol = Date.from(LocalDateTime.from(date.toInstant()).plusDays(1).toInstant(ZoneId.systemDefault().getId()));

                            double value = 70.0 + Math.random() * 5.0;

//                            if (notify) {
//                                Millisecond offset = new Millisecond(lol);
//                                System.out.println(offset);
//                                s1.add(offset, 1);
//                                s2.add(offset, 1);
//                            }

                            s1.add(millisecond, value, false);
                            s2.add(millisecond, 150 + Math.random() * 3.0, notify);

                            counter++;
                        }
                    });

                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private static TimeSeriesCollection createDataset() {
        TimeSeries s1 = new TimeSeries("Voltage");

        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        timeSeriesCollection.addSeries(s1);

        return timeSeriesCollection;
    }

    private static TimeSeriesCollection createDataset2() {
        TimeSeries s2 = new TimeSeries("Speed");

        TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
        timeSeriesCollection.addSeries(s2);

        return timeSeriesCollection;
    }

    private static JFreeChart createChart(XYDataset ds1, XYDataset ds2) {
        // X-Axis
        ValueAxis xAxis = new DateAxis("Test");
        xAxis.setLowerMargin(0.02); // reduce the default margins
        xAxis.setUpperMargin(0.02);

        // 1st Y-Axis
        NumberAxis yAxis1 = new NumberAxis("Test");
        yAxis1.setAutoRangeIncludesZero(false); // override default
        yAxis1.setLowerMargin(0.0);
        yAxis1.setLabelFont(new Font(fontName, Font.BOLD, 14));
        yAxis1.setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        // 2nd Y-Axis
        final NumberAxis yAxis2 = new NumberAxis("Secondary");
        yAxis2.setLabelFont(new Font(fontName, Font.BOLD, 14));
        yAxis2.setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        // 3rd Y-Axis
        final NumberAxis yAxis3 = new NumberAxis("Third");
        yAxis3.setLabelFont(new Font(fontName, Font.BOLD, 14));
        yAxis3.setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        // create renderer
        SamplingXYLineRenderer renderer = new SamplingXYLineRenderer();
        renderer.setDefaultToolTipGenerator(null);
        renderer.setURLGenerator(null);

        XYPlot plot = new XYPlot(ds1, xAxis, yAxis1, renderer) {
            @Override
            public boolean isDomainZoomable() {
                return false;
            }

            @Override
            public boolean isRangeZoomable() {
                return false;
            }
        };
        plot.setDomainPannable(true);
        plot.setDomainCrosshairVisible(true);

        plot.setRangePannable(true);
        plot.setRangeCrosshairVisible(true);

        // add add second axis
        plot.setRangeAxis(1, yAxis2);
        plot.setRangeAxis(2, yAxis3);

        // add second dataset
        plot.setDataset(1, ds2);
        plot.mapDatasetToRangeAxis(1, 1);

        JFreeChart chart = new JFreeChart("SolarCar Test", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));

        // legend
        chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);

        return chart;

    }
}