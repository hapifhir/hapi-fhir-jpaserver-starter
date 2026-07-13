package ca.uhn.fhir.jpa.starter.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.jpa.starter.interceptors.CacheService.CacheEnum;

/**
 * Unit test murni untuk {@link CacheService}, termasuk method
 * {@link CacheService#invalidate(CacheEnum, Object)} yang ditambahkan untuk
 * mendukung cache invalidation per-organisasi lewat
 * {@code /internal/consent-cache/invalidate} (dipanggil oleh consent registry
 * setiap ada perubahan Consent/Provision).
 */
class CacheServiceTest {

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService();
    }

    @Test
    @DisplayName("put lalu getIfPresent mengembalikan value yang sama")
    void putThenGet_returnsSameValue() {
        cacheService.put(CacheEnum.CONSENT_LIST, "org-1", "dummy-consent-list-org-1");

        String result = cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-1");

        assertEquals("dummy-consent-list-org-1", result);
    }

    @Test
    @DisplayName("getIfPresent untuk key yang belum pernah di-put mengembalikan null")
    void getIfPresent_missingKey_returnsNull() {
        String result = cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-yang-tidak-ada");

        assertNull(result);
    }

    @Test
    @DisplayName("get() dengan supplier: cache miss memanggil supplier, cache hit tidak")
    void get_withSupplier_loadsOnceAndCaches() {
        int[] callCount = {0};

        String first = cacheService.get(CacheEnum.CONSENT_LIST, "org-2", key -> {
            callCount[0]++;
            return "loaded-for-" + key;
        });
        String second = cacheService.get(CacheEnum.CONSENT_LIST, "org-2", key -> {
            callCount[0]++;
            return "loaded-for-" + key;
        });

        assertEquals("loaded-for-org-2", first);
        assertEquals("loaded-for-org-2", second);
        assertEquals(1, callCount[0], "Supplier cuma boleh dipanggil sekali - kedua kali harus cache hit");
    }

    // -------------------------------------------------------------------
    // invalidate(CacheEnum, key) - method baru, dipakai endpoint invalidasi
    // -------------------------------------------------------------------

    @Test
    @DisplayName("invalidate satu key: key itu hilang, key lain di cache yang sama tetap ada")
    void invalidate_removesOnlyTargetKey() {
        cacheService.put(CacheEnum.CONSENT_LIST, "org-a", "consent-list-a");
        cacheService.put(CacheEnum.CONSENT_LIST, "org-b", "consent-list-b");

        cacheService.invalidate(CacheEnum.CONSENT_LIST, "org-a");

        assertNull(cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-a"));
        assertEquals("consent-list-b", cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-b"),
                "invalidate satu key tidak boleh ikut membuang key lain (bukan invalidateAll)");
    }

    @Test
    @DisplayName("invalidate key yang tidak pernah ada tidak melempar exception")
    void invalidate_nonExistentKey_doesNotThrow() {
        cacheService.invalidate(CacheEnum.CONSENT_LIST, "org-yang-tidak-pernah-di-put");
        // tidak ada assertion tambahan - cukup pastikan tidak exception
    }

    @Test
    @DisplayName("invalidate di satu tipe cache tidak mempengaruhi tipe cache lain dengan key sama")
    void invalidate_doesNotCrossCacheTypes() {
        cacheService.put(CacheEnum.CONSENT_LIST, "123", "consent-value");
        cacheService.put(CacheEnum.ENCOUNTER, "123", "encounter-value");

        cacheService.invalidate(CacheEnum.CONSENT_LIST, "123");

        assertNull(cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "123"));
        assertEquals("encounter-value", cacheService.getIfPresent(CacheEnum.ENCOUNTER, "123"),
                "CacheEnum.ENCOUNTER dengan key yang sama seharusnya tidak ikut ke-invalidate");
    }

    // -------------------------------------------------------------------
    // invalidateCaches / invalidateAllCaches (perilaku existing, tetap dijaga)
    // -------------------------------------------------------------------

    @Test
    @DisplayName("invalidateCaches(CacheEnum) membuang SEMUA entry di cache tipe itu")
    void invalidateCaches_clearsAllEntriesOfThatType() {
        cacheService.put(CacheEnum.CONSENT_LIST, "org-a", "a");
        cacheService.put(CacheEnum.CONSENT_LIST, "org-b", "b");

        cacheService.invalidateCaches(CacheEnum.CONSENT_LIST);

        assertNull(cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-a"));
        assertNull(cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-b"));
    }

    @Test
    @DisplayName("invalidateAllCaches membuang entry di SEMUA tipe cache")
    void invalidateAllCaches_clearsEverything() {
        cacheService.put(CacheEnum.CONSENT_LIST, "org-a", "a");
        cacheService.put(CacheEnum.ENCOUNTER, "enc-1", "b");
        cacheService.put(CacheEnum.EPISODE_OF_CARE, "eoc-1", "c");
        cacheService.put(CacheEnum.SERVICE_REQUEST, "sr-1", "d");

        cacheService.invalidateAllCaches();

        assertNull(cacheService.getIfPresent(CacheEnum.CONSENT_LIST, "org-a"));
        assertNull(cacheService.getIfPresent(CacheEnum.ENCOUNTER, "enc-1"));
        assertNull(cacheService.getIfPresent(CacheEnum.EPISODE_OF_CARE, "eoc-1"));
        assertNull(cacheService.getIfPresent(CacheEnum.SERVICE_REQUEST, "sr-1"));
    }

    // -------------------------------------------------------------------
    // CacheEnum.fromResourceType - regresi bug getName() vs getSimpleName()
    // -------------------------------------------------------------------

    @Test
    @DisplayName("REGRESI: fromResourceType harus dipanggil dengan simple name, bukan FQCN")
    void fromResourceType_withSimpleName_resolvesCorrectly() {
        assertEquals(CacheEnum.ENCOUNTER, CacheEnum.fromResourceType("Encounter"));
        assertEquals(CacheEnum.SERVICE_REQUEST, CacheEnum.fromResourceType("ServiceRequest"));
        assertEquals(CacheEnum.EPISODE_OF_CARE, CacheEnum.fromResourceType("EpisodeOfCare"));
    }

    @Test
    @DisplayName("fromResourceType dengan fully-qualified class name mengembalikan null (bukti kenapa LoaderWithCache wajib pakai getSimpleName())")
    void fromResourceType_withFullyQualifiedName_returnsNull() {
        assertNull(CacheEnum.fromResourceType("org.hl7.fhir.r4.model.Encounter"),
                "Ini bukti nyata bug lama: clz.getName() (FQCN) tidak akan pernah match, harus clz.getSimpleName()");
    }

    @Test
    @DisplayName("fromResourceType utk resource type yang tidak dikenal mengembalikan null")
    void fromResourceType_unknownType_returnsNull() {
        assertNull(CacheEnum.fromResourceType("Patient"));
    }
}
