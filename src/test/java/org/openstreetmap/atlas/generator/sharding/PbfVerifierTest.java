package org.openstreetmap.atlas.generator.sharding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.generator.tools.streaming.ResourceFileSystem;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;

public class PbfVerifierTest
{

    public static final String PBF = "resource://test/pbf";
    public static final String PBF_380 = PBF + "/10/313/380/10-313-380.pbf";
    public static final String PBF_381 = PBF + "/10/313/381/10-313-381.pbf";

    static
    {
        addResource(PBF_380, "10-313-380.pbf");
        addResource(PBF_381, "10-313-381.pbf");
    }

    private static void addResource(final String path, final String name)
    {
        ResourceFileSystem.addResource(path,
                new InputStreamResource(() -> PbfVerifierTest.class.getResourceAsStream(name)));
    }

    private static Resource resourceForName(final ResourceFileSystem resourceFileSystem,
            final String path)
    {
        final Decompressor decompressor = Decompressor.NONE;
        final Resource res = new InputStreamResource(() ->
        {
            try
            {
                return resourceFileSystem.open(new Path(path));
            }
            catch (final Exception e)
            {
                throw new CoreException("Unable to open Resource in ResourceFileSystem");
            }
        }).withDecompressor(decompressor);
        return res;
    }

    @Test
    public void testParseSlippyTileFile()
    {
        final PbfVerifier pbfVerifier = new PbfVerifier();
        final Resource slippyTileFile = new InputStreamResource(
                () -> PbfVerifierTest.class.getResourceAsStream("testSlippyTile"));
        final Resource pbf1 = new InputStreamResource(
                () -> PbfVerifierTest.class.getResourceAsStream("10-313-380.pbf"));
        final Resource pbf2 = new InputStreamResource(
                () -> PbfVerifierTest.class.getResourceAsStream("10-313-381.pbf"));
        final HashMap<String, Rectangle> shardToBounds = PbfVerifier
                .parseSlippyTileFile(slippyTileFile);
        final List<Resource> pbfFiles = new ArrayList<>();
        try (ResourceFileSystem resFileSystem = new ResourceFileSystem())
        {
            // pbfFiles.add(resourceForName(resFileSystem,
            // "resource://test/pbf/10/313/380/10-313-380.pbf"));
            // pbfFiles.add(resourceForName(resFileSystem,
            // "resource://test/pbf/10/313/381/10-313-381.pbf"));
            pbfFiles.add(pbf1);
            pbfFiles.add(pbf2);
            System.out.println(shardToBounds);
            pbfFiles.forEach(file -> System.out.println(file.getName()));
            pbfVerifier.buildAllPbfs(pbfFiles, shardToBounds);
            System.out.println("Woo!");
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            System.out.print("Yikes!");
        }
    }

}
