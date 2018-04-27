/**
 * Build-Info Maven Plugin.
 */
package info.bluespot.plugins;

/**
 * This class represents an Nexus artifact.
 */
public class MavenUploadedArtifact
{
    /**
     * GroupId.
     */
    private String groupId = null;

    /**
     * ArtifactId.
     */
    private String artifactId = null;

    /**
     * Version.
     */
    private String version = null;

    /**
     * Classifier.
     */
    private String classifier = null;

    /**
     * Type.
     */
    private String type = null;

    /**
     * URL.
     */
    private String url = null;

    /**
     * Constructor.
     */
    public MavenUploadedArtifact( String groupId, String artifactId, String version, String classifier, String type,
                                  String url )
    {
        this.setArtifactId( artifactId );
        this.setGroupId( groupId );
        this.setVersion( version );
        this.setClassifier( classifier );
        this.setType( type );
        this.setUrl( url );
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getClassifier()
    {
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        this.classifier = classifier;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl( String url )
    {
        this.url = url;
    }
}
