import datechooser.beans.DateChooserCombo;
import entidades.TemperaturaRegistro;
import Servicios.TemperaturaServicio;

import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

public class FrmTemperatura extends JFrame {

    private DateChooserCombo dccDesde, dccHasta, dccConsulta;
    private JPanel pnlEstadisticas;
    private JTextArea txtResultados;
    private JTabbedPane tabs;
    private TemperaturaServicio servicio;

    public FrmTemperatura() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Temperaturas por Ciudad");
        setSize(750, 450);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);


        
        servicio = new TemperaturaServicio();
        servicio.importarDatos("src/Datos/Temperaturas.csv");

        JToolBar barra = new JToolBar();

        JButton btnPromedios = new JButton(new ImageIcon(getClass().getResource("/iconos/Grafica.png")));
        btnPromedios.setToolTipText("Ver promedios por ciudad");
        btnPromedios.addActionListener(this::mostrarPromedios);
        barra.add(btnPromedios);

        JButton btnConsulta = new JButton(new ImageIcon(getClass().getResource("/iconos/Datos.png")));
        btnConsulta.setToolTipText("Consultar ciudad más y menos calurosa");
        btnConsulta.addActionListener(this::consultarExtremos);
        barra.add(btnConsulta);

        JButton btnEstudiante = new JButton("Estudiante");
        btnEstudiante.setToolTipText("Información del estudiante");
        btnEstudiante.addActionListener(evt -> mostrarDatosEstudiante());
        barra.add(btnEstudiante);


        JPanel pnlContenedor = new JPanel();
        pnlContenedor.setLayout(new BoxLayout(pnlContenedor, BoxLayout.Y_AXIS));

        JPanel pnlFiltros = new JPanel();
        pnlFiltros.setPreferredSize(new Dimension(pnlFiltros.getWidth(), 50));
        pnlFiltros.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pnlFiltros.setLayout(null);

        JLabel lblDesde = new JLabel("Desde:");
        lblDesde.setBounds(10, 10, 50, 25);
        pnlFiltros.add(lblDesde);

        dccDesde = new DateChooserCombo();
        dccDesde.setBounds(60, 10, 120, 25);
        pnlFiltros.add(dccDesde);

        JLabel lblHasta = new JLabel("Hasta:");
        lblHasta.setBounds(190, 10, 50, 25);
        pnlFiltros.add(lblHasta);

        dccHasta = new DateChooserCombo();
        dccHasta.setBounds(240, 10, 120, 25);
        pnlFiltros.add(dccHasta);

        JLabel lblConsulta = new JLabel("Consulta:");
        lblConsulta.setBounds(370, 10, 70, 25);
        pnlFiltros.add(lblConsulta);

        dccConsulta = new DateChooserCombo();
        dccConsulta.setBounds(440, 10, 120, 25);
        pnlFiltros.add(dccConsulta);

        txtResultados = new JTextArea(10, 50);
        txtResultados.setEditable(false);
        txtResultados.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtResultados.setAlignmentY(Component.CENTER_ALIGNMENT);
        txtResultados.setLineWrap(true);
        txtResultados.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtResultados);

        JPanel pnlResultadosContenedor = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlResultadosContenedor.add(scroll);

        pnlEstadisticas = new JPanel(new BorderLayout());
        pnlEstadisticas.add(pnlResultadosContenedor, BorderLayout.CENTER);

        tabs = new JTabbedPane();
        tabs.addTab("Gráfica", null);
        tabs.addTab("Resultados", pnlEstadisticas);

        pnlContenedor.add(pnlFiltros);
        pnlContenedor.add(tabs);

        getContentPane().add(barra, BorderLayout.NORTH);
        getContentPane().add(pnlContenedor, BorderLayout.CENTER);
        

        addWindowListener(new java.awt.event.WindowAdapter() {
    
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        int opcion = JOptionPane.showConfirmDialog(
                FrmTemperatura.this,
                "¿Seguro que desea salir?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (opcion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
});




    }

    private void mostrarPromedios(ActionEvent e) {
        if (dccDesde.getSelectedDate() == null || dccHasta.getSelectedDate() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar ambas fechas.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate desde = dccDesde.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hasta = dccHasta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (desde.isAfter(hasta)) {
            JOptionPane.showMessageDialog(this, "'Desde' no puede ser posterior a 'Hasta'.", "Rango inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, Double> promedios = servicio.obtenerPromedios(desde, hasta);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        promedios.forEach((ciudad, temperatura) -> {
            dataset.addValue(temperatura, "Promedio", ciudad);
        });

        JFreeChart chart = ChartFactory.createBarChart(
                "Promedio de Temperaturas por Ciudad",
                "Ciudad",
                "Temperatura (°C)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));

        JPanel pnlGrafica = new JPanel(new BorderLayout());
        pnlGrafica.add(chartPanel, BorderLayout.CENTER);

        tabs.setComponentAt(0, pnlGrafica);
        tabs.setSelectedIndex(0);
    }

    private void mostrarDatosEstudiante() {
    JOptionPane.showMessageDialog(
            this,
            "Nombre: Juan Jose Gomez Castaño\nCedula: 1001446730\nCorreo: juan.gomez132@udea.edu.co",
            "Datos del Estudiante",
            JOptionPane.INFORMATION_MESSAGE
    );
}


    private void consultarExtremos(ActionEvent e) {
        if (dccConsulta.getSelectedDate() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una fecha de consulta.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate consulta = dccConsulta.getSelectedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Optional<TemperaturaRegistro> masCalurosa = servicio.buscarMaxima(consulta);
        Optional<TemperaturaRegistro> masFria = servicio.buscarMinima(consulta);

        StringBuilder sb = new StringBuilder("Consulta de temperaturas para: ").append(consulta).append("\n\n");
        sb.append("Ciudad más calurosa: ")
                .append(masCalurosa.map(TemperaturaRegistro::toString).orElse("Sin datos")).append("\n");
        sb.append("Ciudad más fría: ")
                .append(masFria.map(TemperaturaRegistro::toString).orElse("Sin datos")).append("\n");

        txtResultados.setText(sb.toString());

        pnlEstadisticas.revalidate();
        pnlEstadisticas.repaint();

        tabs.setSelectedIndex(1);
    }

    
}
