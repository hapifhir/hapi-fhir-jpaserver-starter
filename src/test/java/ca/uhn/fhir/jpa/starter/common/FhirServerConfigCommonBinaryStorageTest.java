package ca.uhn.fhir.jpa.starter.common;

import ca.uhn.fhir.jpa.binstore.DatabaseBinaryContentStorageSvcImpl;
import ca.uhn.fhir.jpa.binstore.FilesystemBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.starter.AppProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FhirServerConfigCommonBinaryStorageTest {

	@TempDir
	Path tempDir;

	private FhirServerConfigCommon newConfig() {
		return new FhirServerConfigCommon(new AppProperties());
	}

	@Test
	void defaultsToDatabaseImplementation() {
		AppProperties props = new AppProperties();

		Object svc = binaryStorageSvc(props);

		assertThat(svc).isInstanceOf(DatabaseBinaryContentStorageSvcImpl.class);
	}

	@Test
	void filesystemModeUsesDefaultMinimumWhenUnspecified() throws Exception {
		AppProperties props = new AppProperties();
		props.setBinary_storage_mode(AppProperties.BinaryStorageMode.FILESYSTEM);
		Path baseDir = tempDir.resolve("fs-default");
		Files.createDirectories(baseDir);
		props.setBinary_storage_filesystem_base_directory(baseDir.toString());

		FilesystemBinaryStorageSvcImpl svc = filesystemBinaryStorageSvc(props);

		assertThat(svc.getMinimumBinarySize()).isEqualTo(102_400);
	}

	@Test
	void filesystemModeHonoursExplicitMinimum() throws Exception {
		AppProperties props = new AppProperties();
		props.setBinary_storage_mode(AppProperties.BinaryStorageMode.FILESYSTEM);
		props.setInline_resource_storage_below_size(4096);
		Path baseDir = tempDir.resolve("fs-min-explicit");
		Files.createDirectories(baseDir);
		props.setBinary_storage_filesystem_base_directory(baseDir.toString());

		FilesystemBinaryStorageSvcImpl svc = filesystemBinaryStorageSvc(props);

		assertThat(svc.getMinimumBinarySize()).isEqualTo(4096);
	}

	@Test
	void filesystemModeSupportsZeroMinimumWhenExplicit() throws Exception {
		AppProperties props = new AppProperties();
		props.setBinary_storage_mode(AppProperties.BinaryStorageMode.FILESYSTEM);
		props.setInline_resource_storage_below_size(0);
		Path baseDir = tempDir.resolve("fs-zero");
		Files.createDirectories(baseDir);
		props.setBinary_storage_filesystem_base_directory(baseDir.toString());

		FilesystemBinaryStorageSvcImpl svc = filesystemBinaryStorageSvc(props);

		assertThat(svc.getMinimumBinarySize()).isZero();
	}

	@Test
	void filesystemModeRequiresBaseDirectory() {
		AppProperties props = new AppProperties();
		props.setBinary_storage_mode(AppProperties.BinaryStorageMode.FILESYSTEM);

		assertThatThrownBy(() -> filesystemBinaryStorageSvc(props))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("binary_storage_filesystem_base_directory");
	}

	private Object binaryStorageSvc(AppProperties props) {
		FhirServerConfigCommon config = newConfig();
		return config.binaryStorageSvc(
				props,
				provider(() -> config.databaseBinaryStorageSvc(props)),
				provider(() -> config.filesystemBinaryStorageSvc(props)));
	}

	private FilesystemBinaryStorageSvcImpl filesystemBinaryStorageSvc(AppProperties props) {
		return (FilesystemBinaryStorageSvcImpl) binaryStorageSvc(props);
	}

	private static <T> ObjectProvider<T> provider(Supplier<T> supplier) {
		return new ObjectProvider<>() {
			@Override
			public T getObject(Object... args) {
				return supplier.get();
			}

			@Override
			public T getObject() {
				return supplier.get();
			}

			@Override
			public T getIfAvailable() {
				return supplier.get();
			}

			@Override
			public T getIfUnique() {
				return supplier.get();
			}
		};
	}
}
