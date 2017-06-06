package main;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jfreechart.SlidingXYDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.SamplingXYLineRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

//TODO Farben
//TODO Mehrere Axen
//TODO SlidingXYDataSet
//TODO - Zoom nur jeden x-ten Wert anzeigen
//TODO - Scroll

public class MainController implements Initializable {
    final String fontName = "Palatino";
    private boolean run = true;
    private static XYPlot plot;
    private DateAxis xAxis;

    @FXML
    private BorderPane rootPane;
    @FXML
    private ScrollBar xScrollBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TimeSeries s1 = new TimeSeries("Voltage");
//        s1.setMaximumItemAge(10000);

        TimeSeries s2 = new TimeSeries("Speed");
//        s2.setMaximumItemAge(10000);

        TimeSeriesCollection tsc1 = new TimeSeriesCollection();
        tsc1.addSeries(s1);
        tsc1.addSeries(s2);

        TimeSeriesCollection tsc2 = new TimeSeriesCollection();
        tsc2.addSeries(s2);

        xScrollBar.setMin(new Date().getTime());
        xScrollBar.valueProperty().addListener(ae -> {
                    onXScrollBarMoved();
                }
        );
//        Date date = new Date();
//        int time = (int)date.toInstant().getEpochSecond();
//        SlidingXYDataset sds1 = new SlidingXYDataset(tsc1, time, time + 10);
//        SlidingXYDataset sds2 = new SlidingXYDataset(tsc2, time, time + 10);

        JFreeChart chart = createChart(tsc1, tsc2);

        ChartViewer viewer = new ChartViewer(chart);
        viewer.setOnContextMenuRequested(null);

        viewer.getCanvas().addChartMouseListener(new ChartMouseListenerFX() {
            @Override
            public void chartMouseClicked(ChartMouseEventFX event) {
                if (event.getTrigger().getButton() == MouseButton.SECONDARY) {
                    if (event.getEntity() instanceof AxisEntity) {
                        AxisEntity axisEntity = (AxisEntity) event.getEntity();

                        if (axisEntity.getAxis() == plot.getDomainAxis()) {
                            onXAxisClicked(axisEntity.getAxis());
                        } else {
                            onYAxisClicked(axisEntity.getAxis());
                        }
                    } else if (event.getEntity() instanceof LegendItemEntity) {
                        LegendItemEntity legendItemEntity = (LegendItemEntity) event.getEntity();
                        System.out.println("entity: " + (legendItemEntity == null));
                        onLegendItemClicked(legendItemEntity);
                    }
                }
            }

            @Override
            public void chartMouseMoved(ChartMouseEventFX event) {

            }
        });

//        viewer.setPrefSize(rootPane.getPrefWidth(), rootPane.getPrefHeight());
        // we need to set the size, to auto-resize the CartViewer when the rootPane is resized
        rootPane.setPrefSize(viewer.getPrefWidth(), viewer.getPrefHeight());
        rootPane.setCenter(viewer);

        Task task = new Task() {
            private int counter = 1;

            @Override
            protected Void call() throws Exception {
                while (run) {
                    Platform.runLater(new Runnable() {
                        TimeSeries s1 = (TimeSeries) tsc1.getSeries().get(0);
                        TimeSeries s2 = (TimeSeries) tsc1.getSeries().get(1);

                        @Override
                        public void run() {
                            boolean notify = counter % 5 == 0;

                            Date date = new Date();

                            xScrollBar.setMax(date.getTime());

                            Millisecond millisecond = new Millisecond(date);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            cal.add(Calendar.SECOND, -10);
//                            xAxis.setRange(cal.getTime(), date);

                            double value = 70.0 + Math.random() * 5.0;

                            s1.add(millisecond, value, false);
                            s2.add(millisecond, 150 + Math.random() * 3.0, notify);


                            counter++;
                        }
                    });

                    try {
                        Thread.sleep(200);
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

    @FXML
    void onXScrollBarMoved() {
        System.out.println("onXScrollBarMoved");
        xAxis.setRange(xScrollBar.getValue(), xScrollBar.getValue() + 10000);
    }

    private void onXAxisClicked(Axis xAxis) {
//        System.out.println("onYAxisClicked");
//        if (xAxis instanceof DateAxis) {
//            DateAxis axis = (DateAxis) valueAxis;
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("XAxisPopupView.fxml"));
//                BorderPane rootPane = loader.load();
//
//                YAxisPopupController controller = (YAxisPopupController) loader.<YAxisPopupController>getController();
//                controller.setAxis(axis);
//
//                Stage st = new Stage();
//                Scene scene = new Scene(rootPane);
//                st.setScene(scene);
//                st.setTitle(axis.getLabel());
//                st.setResizable(false);
//                st.show();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void onYAxisClicked(Axis yAxis) {
        if (yAxis instanceof NumberAxis) {
            NumberAxis axis = (NumberAxis) yAxis;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("YAxisPopupView.fxml"));
                BorderPane rootPane = loader.load();

                YAxisPopupController controller = (YAxisPopupController) loader.<YAxisPopupController>getController();
                controller.setAxis(axis);

                Stage st = new Stage();
                Scene scene = new Scene(rootPane);
                st.setScene(scene);
                st.setTitle(axis.getLabel());
                st.setResizable(false);
                st.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onLegendItemClicked(LegendItemEntity legendItemEntity) {
        System.out.println("legend item clicked");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("LegendItemPopupView.fxml"));
            BorderPane rootPane = loader.load();

            LegendItemPopupController controller = (LegendItemPopupController) loader.<LegendItemPopupController>getController();
            controller.setPlot(plot);
            controller.setLegendItemEntity(legendItemEntity);

            Stage st = new Stage();
            Scene scene = new Scene(rootPane);
            st.setScene(scene);
            st.setTitle(legendItemEntity.getSeriesKey().toString());
            st.setResizable(false);
            st.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JFreeChart createChart(XYDataset ds1, XYDataset ds2) {
        // X-Axis
        xAxis = new DateAxis("Test");
        xAxis.setLowerMargin(0.01); // reduce the default margins
        xAxis.setUpperMargin(0.01);

        // 1st Y-Axis
        final NumberAxis yAxis1 = new NumberAxis("Voltage");
        yAxis1.setAutoRangeIncludesZero(false); // override default
        yAxis1.setLowerMargin(0.0);
        yAxis1.setLabelFont(new Font(fontName, Font.BOLD, 14));
        yAxis1.setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        // 2nd Y-Axis
        final NumberAxis yAxis2 = new NumberAxis("Speed");
        yAxis2.setAutoRangeIncludesZero(false);
        yAxis2.setLabelFont(new Font(fontName, Font.BOLD, 14));
        yAxis2.setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        // 3rd Y-Axis
        final NumberAxis yAxis3 = new NumberAxis("Third");
        yAxis3.setLabelFont(new Font(fontName, Font.BOLD, 14));
        yAxis3.setTickLabelFont(new Font(fontName, Font.PLAIN, 12));

        plot = new XYPlot() {
//            @Override
//            public boolean isDomainZoomable() {
//                return false;
//            }
//
//            @Override
//            public boolean isRangeZoomable() {
//                return false;
//            }
        };
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis1);

        // create renderer
        SamplingXYLineRenderer renderer = new SamplingXYLineRenderer() {
            @Override
            public LegendItem getLegendItem(int datasetIndex, int series) {
                LegendItem legend = super.getLegendItem(datasetIndex, series);
                LegendItem newLegend = new LegendItem(legend.getLabel(),
                        legend.getDescription(),
                        legend.getToolTipText(),
                        legend.getURLText(), Plot.DEFAULT_LEGEND_ITEM_BOX, legend.getFillPaint());
                newLegend.setSeriesKey(legend.getSeriesKey());
                newLegend.setDataset(legend.getDataset());
                return newLegend;
            }
        };
        renderer.setDefaultToolTipGenerator(null);
        renderer.setURLGenerator(null);
        plot.setDataset(0, ds1);
        plot.setRenderer(0, renderer);

        plot.setDomainPannable(true);
        plot.setDomainCrosshairVisible(true);

        plot.setRangePannable(true);
        plot.setRangeCrosshairVisible(true);

        // add add second axis
        plot.setRangeAxis(1, yAxis2);
        plot.setRangeAxis(2, yAxis3);

        // add second dataset
        SamplingXYLineRenderer renderer2 = new SamplingXYLineRenderer() {
            @Override
            public LegendItem getLegendItem(int datasetIndex, int series) {
                LegendItem legend = super.getLegendItem(datasetIndex, series);
                LegendItem newLegend = new LegendItem(legend.getLabel(), legend.getDescription(), legend.getToolTipText(), legend.getURLText(), Plot.DEFAULT_LEGEND_ITEM_BOX, legend.getFillPaint());
                newLegend.setSeriesKey(legend.getSeriesKey());
                newLegend.setDataset(legend.getDataset());
                return newLegend;
            }
        };

        renderer2.setDefaultToolTipGenerator(null);
        renderer2.setURLGenerator(null);

        plot.setDataset(1, ds2);
        plot.setRenderer(1, renderer2);
        plot.mapDatasetToRangeAxis(1, 1);

        JFreeChart chart = new JFreeChart("SolarCar Test", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));

        // legend
        chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        chart.getLegend().setFrame(BlockBorder.NONE);
//        chart.getLegend().set
        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);
//chart.getLegend().
        return chart;
    }
}