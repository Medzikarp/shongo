package cz.cesnet.shongo.controller.api;

import cz.cesnet.shongo.api.DataMap;
import cz.cesnet.shongo.api.Recording;

/**
 * Represents {@link Recording} which belongs to some {@link Executable} and which is located in some recording folder.
 *
 * @author Martin Srom <martin.srom@cesnet.cz>
 */
public class ResourceRecording extends Recording
{
    /**
     * Identifier of {@link DeviceResource} where the {@link ResourceRecording} is located.
     */
    private String resourceId;

    /**
     * Constructor.
     */
    public ResourceRecording()
    {
    }

    /**
     * Constructor.
     *
     * @param resourceId sets the {@link #resourceId}
     * @param recording  to initialize {@link Recording}
     */
    public ResourceRecording(String resourceId, Recording recording)
    {
        this.resourceId = resourceId;

        setId(recording.getId());
        setRecordingFolderId(recording.getRecordingFolderId());
        setName(recording.getName());
        setDescription(recording.getDescription());
        setUrl(recording.getUrl());
        setDownloadableUrl(recording.getDownloadableUrl());
        setEditableUrl(recording.getEditableUrl());
        setBeginDate(recording.getBeginDate());
        setDuration(recording.getDuration());
        setFileName(recording.getFileName());
    }

    /**
     * @return {@link #resourceId}
     */
    public String getResourceId()
    {
        return resourceId;
    }

    /**
     * @param resourceId sets the {@link #resourceId}
     */
    public void setResourceId(String resourceId)
    {
        this.resourceId = resourceId;
    }

    public static final String EXECUTABLE_ID = "executableId";

    @Override
    public DataMap toData()
    {
        DataMap dataMap = super.toData();
        dataMap.set(EXECUTABLE_ID, resourceId);
        return dataMap;
    }

    @Override
    public void fromData(DataMap dataMap)
    {
        super.fromData(dataMap);
        resourceId = dataMap.getString(EXECUTABLE_ID);
    }
}
