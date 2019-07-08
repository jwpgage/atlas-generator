package org.openstreetmap.atlas.generator.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jamesgage
 */
public class WorldFerryAtlasGenerator extends Command
{
    private static final Switch<String> ATLAS_PATH = new Switch<>("atlasPath",
            "The path to the ferry atlas root directory", value -> value, Optionality.REQUIRED);
    private static final Switch<String> OUTPUT_PATH = new Switch<>("outputPath",
            "The path to the output world ferry atlas", value -> value, Optionality.REQUIRED);
    private static final Logger logger = LoggerFactory.getLogger(WorldFerryAtlasGenerator.class);

    public static void main(final String[] args)
    {
        new WorldFerryAtlasGenerator().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final String atlasPath = (String) command.get(ATLAS_PATH);
        final String outputPath = (String) command.get(OUTPUT_PATH);
        final Map<String, String> configuration = new HashMap<>();
        configuration.put("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
        final List<Resource> atlasFiles = FileSystemHelper.listResourcesRecursively(atlasPath,
                configuration, new AtlasFilePathFilter());
        final MultiAtlas multiAtlas = MultiAtlas.loadFromPackedAtlas(atlasFiles);
        final PackedAtlasCloner packedAtlasCloner = new PackedAtlasCloner();
        final Atlas finalAtlas = packedAtlasCloner.cloneFrom(multiAtlas);
        finalAtlas.save(new File(outputPath));
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ATLAS_PATH, OUTPUT_PATH);
    }
}
