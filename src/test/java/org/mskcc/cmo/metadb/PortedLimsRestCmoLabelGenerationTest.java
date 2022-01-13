package org.mskcc.cmo.metadb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cmo.common.enums.NucleicAcid;
import org.mskcc.cmo.common.enums.SpecimenType;
import org.mskcc.cmo.metadb.config.TestConfiguration;
import org.mskcc.cmo.metadb.model.SampleMetadata;
import org.mskcc.cmo.metadb.model.igo.IgoSampleManifest;
import org.mskcc.cmo.metadb.service.CmoLabelGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Ported tests from SampleTypeCorrectedCmoSampleViewGeneratorTest.java
 * https://bit.ly/2Sqfxi3
 *
 * <p>Reference code:
 * - https://github.com/mskcc/LimsRest : SampleTypeCorrectedCmoSampleViewGeneratorTest.java
 * - https://github.com/mskcc/common-domain : SpecimenType.java, NucleicAcid.java, etc.
 *
 * <p>Notes:
 * - Looks like all samples for a given patient id are returned.
 * - Each sample has its corresponding request id assigned to it.
 * @author ochoaa
 */
@ContextConfiguration(classes = TestConfiguration.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PortedLimsRestCmoLabelGenerationTest {

    @Autowired
    private CmoLabelGeneratorService cmoLabelGeneratorService;

    /**
     * Test CMO label generation for a patient with no samples.
     * Returned label should be the CMO ID with number 1.
     * Ported from test:
     *   whenThereAreNoPatientSamples_shouldReturnCmoIdWithNumber1
     * @throws Exception
     */
    @Test
    public void testPatientNoExistingSamples() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        // change to enum SpecimenType.Xenograft("Xenograft")
        // change to enum NucleicAcid.DNA("DNA")
        IgoSampleManifest sample = getSampleMetadata("4324", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);

        String cmoId = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample, new ArrayList<>());
        Assert.assertEquals("C-1235-X001-d01", cmoId);
    }

    /**
     * Test CMO label generation for a patient with existing sample of same request,
     * same igo id, same specimen type, and same nucleic acid.
     * Returned label should be the CMO ID with number 2.
     * Ported from test:
     *   whenThereIsOnePatientSampleFromSameRequestWithCount1_shouldReturnCmoIdWithNumber2
     * @throws Exception
     */
    @Test
    public void testPatientOneExistingSample() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample = getSampleMetadata("4324", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);

        String existingSampleId = "C-1235-X001-d";
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("5656",
                cmoPatientId, "Xenograft", existingSampleId);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        String cmoId = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample, existingSamples);
        Assert.assertEquals("C-1235-X002-d02", cmoId);
    }

    /**
     * Test CMO label generation for a patient with existing sample of same request,
     * same igo id, same specimen type, and same nucleic acid.
     * Returned label should be the CMO ID with number 2.
     * This is the same test as directly above with the added nucleic acid abbreviation
     * counter.
     * Ported from test:
     *   whenThereIsOnePatientSampleFromSameRequestWithCount1_shouldReturnCmoIdWithNumber2
     * @throws Exception
     */
    @Test
    public void testPatientOneExistingSampleNucAcidCounter() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample = getSampleMetadata("4324", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);

        String existingSampleId = "C-1235-X001-d01";
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("5656",
                cmoPatientId, "Xenograft", existingSampleId);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        String cmoId = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample, existingSamples);
        Assert.assertEquals("C-1235-X002-d02", cmoId);
    }

    /**
     * Test CMO label generation for a patient with existing sample of same igoId
     * and request. Returns label with CMO ID incremented by one.
     * Ported from test:
     *   whenThereIsOnePatientSampleFromSameRequestWithSomeCount_shouldReturnCmoIdWithThisCountPlusOne
     * @throws Exception
     */
    @Test
    public void testPatientOneSampleNextIncrement() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample = getSampleMetadata("4324", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);

        String existingSampleId = "C-1235-X012-d01";
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("5656",
                cmoPatientId, "Xenograft", existingSampleId);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        String cmoId = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample, existingSamples);
        Assert.assertEquals("C-1235-X013-d02", cmoId);
    }

    /**
    * Test CMO label generation for a patient sample from a different request.
    * Sample also is of a different specimen type.
    * Returns label with CMO ID incremented.
    * Ported from test:
    *   whenThereIsOnePatientSampleFromDifferentRequest_shouldReturnCmoIdWithNumber2
     * @throws Exception
    */
    @Test
    public void testPatientOneSampleDiffRequest() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample = getSampleMetadata("4324", cmoPatientId,
                SpecimenType.PDX, NucleicAcid.RNA);

        String existingSampleId = "C-1235-X001-d01";
        String diffRequestId = "1234_A";
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("5656",
                cmoPatientId, "Xenograft", existingSampleId);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        // since nucleic acid abbreviation is different from existing sample, the
        // nucleic acid counter is also '01'
        String cmoId = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample, existingSamples);
        Assert.assertEquals("C-1235-X002-r01", cmoId);
    }

    /**
     * Test CMO label generation for a patient with no samples but sample(s) already
     * exist of the same specimen type and nucleic acid.
     * Returns label with CMO ID incremented by one.
     * Ported from test:
     *   whenThereAreNoPatientSamplesButOneWasAlreadyCreatedFromSameRequestSameSpecimen ...
     *   ... SampleNucl_shouldReturnCmoIdWithNumber2
     * @throws Exception
     */
    @Test
    public void testPatientTwoSamplesSameSpecimenTypeNucleicAcid() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample1 = getSampleMetadata("4324_1", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);
        String cmoId1 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId,
                sample1, new ArrayList<>());
        Assert.assertEquals("C-1235-X001-d01", cmoId1);

        // now we have one existing sample in this request for the same patient
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("4324_1",
                cmoPatientId, "Xenograft", cmoId1);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        IgoSampleManifest sample2 = getSampleMetadata("4324_2", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);
        String cmoId2 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample2, existingSamples);
        Assert.assertEquals("C-1235-X002-d02", cmoId2);
    }

    /**
     * Test CMO label generation for a patient with no samples but sample(s) already
     * exist of the same specimen type but different nucleic acid.
     * Returns label with CMO ID incremented by one.
     * Ported from test:
     *   whenThereAreNoPatientSamplesButOneWasAlreadyCreatedFromSameRequestSameSpecimenDifferent ...
     *   ... Nucl_shouldReturnCmoIdWithNumber2
     * @throws Exception
     */
    @Test
    public void testPatientTwoSamplesSameSpecimenTypeDiffNucleicAcid() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample1 = getSampleMetadata("4324_1", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);
        String cmoId1 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId,
                sample1, new ArrayList<>());
        Assert.assertEquals("C-1235-X001-d01", cmoId1);

        // now we have one existing sample in this request for the same patient
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("4324_1",
                cmoPatientId, "Xenograft", cmoId1);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        IgoSampleManifest sample2 = getSampleMetadata("4324_2", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.RNA);
        String cmoId2 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId, sample2, existingSamples);
        Assert.assertEquals("C-1235-X002-r01", cmoId2);
    }

    /**
     * Test CMO label generation for patient with no samples but one was already created from same request,
     * different specimen type, and same nucleic acid.
     * Returns label with CMO ID incremented by one.
     * Ported from test:
     *   whenThereAreNoPatientSamplesButOneWasAlreadyCreatedFromSameRequestSameSampleType ...
     *   ... _shouldReturnCmoIdWithNumber2
     * @throws Exception
     */
    @Test
    public void testPatientTwoSamplesDiffSpecimenTypeSameRequestNucleicAcid()
            throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample1 = getSampleMetadata("4324_1", cmoPatientId,
                SpecimenType.XENOGRAFT, NucleicAcid.DNA);
        String cmoId1 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId,
                sample1, new ArrayList<>());
        Assert.assertEquals("C-1235-X001-d01", cmoId1);

        // now we have one existing sample in this request for the same patient
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("4324_1",
                cmoPatientId, "Xenograft", cmoId1);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        IgoSampleManifest sample2 = getSampleMetadata("4324_2", cmoPatientId,
                SpecimenType.PDX, NucleicAcid.DNA);
        String cmoId2 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId,
                sample2, existingSamples);
        Assert.assertEquals("C-1235-X002-d02", cmoId2);
    }

    /**
     * Test CMO label generation for patient with no samples but one already created from different
     * request, same sample specimen type, same nucleic acid.
     * Returns label with CMO ID incremented by one.
     * Ported from test:
     *   whenThereAreNoPatientSamplesButOneWasAlreadyCreatedFromDifferentRequestsSampleSpecimen ...
     *   ... _shouldReturnCmoIdWithNumber2
     * @throws Exception
     */
    @Test
    public void testPatientTwoSamplesDiffRequestSameeSpecimenTypeNucleicAcid() throws Exception {
        String requestId = "5432_P";
        String cmoPatientId = "C-1235";

        IgoSampleManifest sample1 = getSampleMetadata("4324_1", cmoPatientId,
                SpecimenType.ORGANOID, NucleicAcid.DNA);
        String cmoId1 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId,
                sample1, new ArrayList<>());
        Assert.assertEquals("C-1235-G001-d01", cmoId1);

        // now we have one existing sample in this request for the same patient
        SampleMetadata existingSample = getSampleMetadataWithCmoLabel("4324_1",
                cmoPatientId, "Organoid", cmoId1);
        // get existing samples for given igo id and request id
        List<SampleMetadata> existingSamples = Arrays.asList(existingSample);

        IgoSampleManifest sample2 = getSampleMetadata("4324_2", cmoPatientId,
                SpecimenType.ORGANOID, NucleicAcid.DNA);
        String cmoId2 = cmoLabelGeneratorService.generateCmoSampleLabel(requestId,
                sample2, existingSamples);
        Assert.assertEquals("C-1235-G002-d02", cmoId2);
    }

    private SampleMetadata getSampleMetadataWithCmoLabel(String igoId, String cmoPatientId,
            String specimenType, String cmoSampleName) {
        SampleMetadata sample = new SampleMetadata();
        sample.setPrimaryId(igoId);
        sample.setCmoPatientId(cmoPatientId);
        sample.setSampleClass(specimenType);
        sample.setCmoSampleName(cmoSampleName);
        return sample;
    }

    private IgoSampleManifest getSampleMetadata(String igoId, String cmoPatientId,
            SpecimenType specimenType, NucleicAcid naToExtract) {
        IgoSampleManifest sample = new IgoSampleManifest();
        sample.setIgoId(igoId);
        sample.setCmoPatientId(cmoPatientId);
        sample.setSpecimenType(specimenType.getValue());

        Map<String, String> cmoSampleIdFields = new HashMap<>();
        cmoSampleIdFields.put("naToExtract", naToExtract.getValue());
        sample.setCmoSampleIdFields(cmoSampleIdFields);
        return sample;
    }
}
