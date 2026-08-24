import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

// Jframe para crear la ventana para la interfaz
public class ComparacionesRedesSociales extends JFrame 
{
    
    //una lista para guardar las filas del archivo csv
    private ArrayList<String[]> datosCsv = new ArrayList<>();
    
    // Componentes de la interfaz
    private JComboBox<String> comboMesInicio;
    private JComboBox<String> comboMesFin;
    private JButton Calcular;
    private JTextArea Resultados;
    
    // arreglo de meses para las elecciones
    private String[] meses = {"ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO"};
                          
    // Constructor de la ventana
    public ComparacionesRedesSociales() 
    {
        setTitle("Análisis de Redes Sociales");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrar 
        
        // diseño del menu
        JPanel panelArriba = new JPanel(new FlowLayout());
        
        panelArriba.add(new JLabel("Primer mes:"));
        comboMesInicio = new JComboBox<>(meses);
        panelArriba.add(comboMesInicio);
        
        panelArriba.add(new JLabel("Ultimo mes:"));
        comboMesFin = new JComboBox<>(meses);
        panelArriba.add(comboMesFin);
        
        Calcular = new JButton("Calcular y Mostrar");
        panelArriba.add(Calcular);
        
        Resultados = new JTextArea();
        Resultados.setEditable(false);
        Resultados.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(Resultados);
        
        // Paneles que se agregan a la ventana
        add(panelArriba, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        
        // Cargar el csv cuando se abra la ventana
        leerArchivoCSV("datos_redes_sociales.csv");
        
      //para el calculo del boton
        Calcular.addActionListener(new ActionListener() 
        {
            
            @Override
            public void actionPerformed(ActionEvent e)            
            {
                hacerCalculos();
            }
        });
    }

    // lee el csv y lo guarda
    private void leerArchivoCSV(String ruta) 
    {
        try 
        {
            BufferedReader br = new BufferedReader(new FileReader(ruta));
            String linea;
            br.readLine(); //no guarda la primera linea
            
            while ((linea = br.readLine()) != null) 
            {
                // Se separa por comas y guardamos el arreglo en la lista
                String[] columnas = linea.split(",");
                datosCsv.add(columnas);
            }
            br.close();
            Resultados.setText("Escoge los meses entre los que se calculara\n");
        } 
        catch (Exception e) 
        {
            Resultados.setText("Error.");                               
        }
    }

    // Busca el valor en el archivo csv
    private int obtenerValor(String red, String concepto, int indiceMes) 
    {
        // Observa las filas
        for (int i = 0; i < datosCsv.size(); i++) 
        {
            String[] fila = datosCsv.get(i);
            
            // Verificamos si la red social y el concepto coinciden
            if (fila[0].trim().equalsIgnoreCase(red) && fila[1].trim().equalsIgnoreCase(concepto)) 
            {
                
                // el 3 es desde donde empiezan los meses al leerlo
                int columnaMes = 3 + indiceMes; 
                
                if (columnaMes < fila.length) 
                {
                    String valorTexto = fila[columnaMes];
                    // Limpiamos el texto 
                    valorTexto = valorTexto.replace("\"", "").replace(",", "").trim();
                    try 
                    {
                        return Integer.parseInt(valorTexto);
                    } 
                    catch (Exception e) 
                    {
                        return 0; // Si está vacío o hay un error de conversión, devolvemos 0
                    }
                }
            }
        }
     return 0;
    }

    // Calculos a realizar
    private void hacerCalculos() 
    {
        int mesInicio = comboMesInicio.getSelectedIndex();
        int mesFin = comboMesFin.getSelectedIndex();
        
        // Validación lógica básica
        if (mesFin < mesInicio) 
        {
            JOptionPane.showMessageDialog(this, "El ultimo mes no puede ser anterior al primero.");
            return;
        }

        StringBuilder resultados = new StringBuilder();
        resultados.append(".......Calculos de ").append(meses[mesInicio]).append(" a ").append(meses[mesFin]).append(" .......\n\n");
        
        // Diferencia de seguidores en Twitter
        int twInicio = obtenerValor("TWITTER", "SEGUIDORES (FOLLOWERS)", mesInicio);
        int twFin = obtenerValor("TWITTER", "SEGUIDORES (FOLLOWERS)", mesFin);
        int difTwitter = twFin - twInicio;
        resultados.append("*** Diferencia Seguidores Twitter: ").append(difTwitter).append("\n");
        
        // Diferencia de visualizaciones en YouTube
        int ytInicio = obtenerValor("YOUTUBE", "VISUALIZACIONES", mesInicio);
        int ytFin = obtenerValor("YOUTUBE", "VISUALIZACIONES", mesFin);
        int difYoutube = ytFin - ytInicio;
        resultados.append("*** Diferencia Visualizaciones YouTube: ").append(difYoutube).append("\n\n");
        
        // Promedio de crecimiento de Twitter y Facebook
        int sumaCrecimientoTw = 0;
        int sumaCrecimientoFb = 0;
        int totalMeses = (mesFin - mesInicio) + 1; 
        
        for (int i = mesInicio; i <= mesFin; i++) 
        {
            sumaCrecimientoTw += obtenerValor("TWITTER", "CRECIMIENTO DE FOLLOWERS", i);
            sumaCrecimientoFb += obtenerValor("FACEBOOK", "CRECIMIENTO (seguidores)", i);
        }
        double promCrecimientoTw = (double) sumaCrecimientoTw / totalMeses;
        double promCrecimientoFb = (double) sumaCrecimientoFb / totalMeses;
        
        resultados.append("*** Promedio de Crecimiento:\n");
        resultados.append("   - Twitter: ").append(String.format("%.2f", promCrecimientoTw)).append("\n");
        resultados.append("   - Facebook: ").append(String.format("%.2f", promCrecimientoFb)).append("\n\n");
        
        // Promedio de Me gusta de YouTube, Twitter y Facebook
        int sumaMgYoutube = 0, sumaMgTwitter = 0, sumaMgFacebook = 0;
        
        for (int i = mesInicio; i <= mesFin; i++) 
        {
            sumaMgYoutube += obtenerValor("YOUTUBE", "ME GUSTA", i);
            sumaMgTwitter += obtenerValor("TWITTER", "ME GUSTA", i);
            sumaMgFacebook += obtenerValor("FACEBOOK", "ME GUSTA EN PUBLICACIONES", i);
        }
        
        resultados.append("*** Promedio de 'Me Gusta':\n");
        resultados.append("   - YouTube: ").append(String.format("%.2f", (double)sumaMgYoutube / totalMeses)).append("\n");
        resultados.append("   - Twitter: ").append(String.format("%.2f", (double)sumaMgTwitter / totalMeses)).append("\n");
        resultados.append("   - Facebook: ").append(String.format("%.2f", (double)sumaMgFacebook / totalMeses)).append("\n");
        
        // Mostramos el texto en el panel
        Resultados.setText(resultados.toString());
    }

    // Método Main para correr el programa
    public static void main(String[] args) 
    {
        
        new ComparacionesRedesSociales().setVisible(true);
    }
}
