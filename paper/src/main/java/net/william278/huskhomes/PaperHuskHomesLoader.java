package net.william278.huskhomes;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import net.william278.annotaml.Annotaml;
import net.william278.annotaml.YamlFile;
import net.william278.annotaml.YamlKey;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class PaperHuskHomesLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolveLibraries(classpathBuilder).stream()
                .map(DefaultArtifact::new)
                .forEach(artifact -> resolver.addDependency(new Dependency(artifact, null)));
        resolver.addRepository(new RemoteRepository.Builder(
                "maven", "default", "https://repo.maven.apache.org/maven2/"
        ).build());

        classpathBuilder.addLibrary(resolver);
    }

    @NotNull
    private static List<String> resolveLibraries(@NotNull PluginClasspathBuilder classpathBuilder) {
        try (InputStream input = PaperHuskHomesLoader.class.getClassLoader().getResourceAsStream("paper-libraries.yml")) {
            return Annotaml.create(PaperLibraries.class, Objects.requireNonNull(input)).get().libraries;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @YamlFile(header = "Dependencies for HuskHomes on Paper")
    public static class PaperLibraries {

        @YamlKey("libraries")
        private List<String> libraries;

        @SuppressWarnings("unused")
        private PaperLibraries() {
        }

    }

}
