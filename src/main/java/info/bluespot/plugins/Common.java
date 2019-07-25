/**
 * Build-Info Maven Plugin.
 */
package info.bluespot.plugins;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoFailureException;

/**
 * Just few common methods.
 */
public class Common
{
    /**
     * Output format 'Shell export'
     */
    public static String OUTPUT_SH_EXPORT = "sh";

    /**
     * Output format: JSON
     */
    public static String OUTPUT_JSON = "json";

    /**
     * Output format: CSV
     */
    public static String OUTPUT_CSV = "csv";

    /**
     * Output format: YAML
     */
    public static String OUTPUT_YAML = "yml";

    /**
     * Output format: XML
     */
    public static String OUTPUT_XML = "xml";

    /**
     * Obtiene el formato de salida, ya sea de la propiedad 'outputFormat' o de la extensión del fichero.
     */
    public static String getOutputFormat( String outputFile )
    {
        String format = null;

        if ( outputFile.endsWith( ".json" ) )
        {
            format = "json";
        }
        else if ( outputFile.endsWith( ".csv" ) )
        {
            format = "csv";
        }
        else if ( outputFile.endsWith( ".xml" ) )
        {
            format = "xml";
        }
        else if ( outputFile.endsWith( ".yml" ) || outputFile.endsWith( ".yaml" ))
        {
            format = "yml";
        }
        else if ( outputFile.endsWith( ".sh" ) )
        {
            format = "sh";
        }
        return ( format );
    }

    /**
     * Writes the output to a file.
     * @throws MojoFailureException An exception while writting the file.
     */
    public static void writeToFile( ArrayList<String> output, String outputFile ) throws MojoFailureException
    {
        if ( outputFile != null )
        {
            // Write to a file
            try
            {
                BufferedWriter writer = new BufferedWriter( new FileWriter( outputFile ) );
                for ( String line : output )
                {
                    writer.write( line );
                    writer.newLine();
                }
                writer.close();
            }
            catch ( IOException e )
            {
                throw new MojoFailureException( "Error while writting the file: '" + outputFile + "'", e );
            }
        }
        else
        {
            // Show the information to STDOUT
            for ( String line : output )
            {
                System.out.println( line );
            }
        }
    }
}
