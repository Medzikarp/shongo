package cz.cesnet.shongo.connector.api;

/**
 * Description of a media type.
 * <p/>
 * Any MIME Media Type listed by IANA, e.g. image/jpeg.
 *
 * @author Ondrej Bouda <ondrej.bouda@cesnet.cz>
 * @see <a href="http://www.iana.org/assignments/media-types/index.html">IANA MIME Media Type list</a>
 */
public class ContentType
{
    private String type;
    private String subtype;

    /**
     * @return textual name of the subtype (e.g., "jpeg" or "html")
     */
    public String getSubtype()
    {
        return subtype;
    }

    /**
     * @param subtype textual name of the subtype (e.g., "jpeg" or "html")
     */
    public void setSubtype(String subtype)
    {
        this.subtype = subtype;
    }

    /**
     * @return textual name of the type (e.g., "image" or "text")
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type textual name of the type (e.g., "image" or "text")
     */
    public void setType(String type)
    {
        this.type = type;
    }
}
