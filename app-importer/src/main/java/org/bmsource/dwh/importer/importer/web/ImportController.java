package org.bmsource.dwh.importer.importer.web;

import org.apache.commons.io.IOUtils;
import org.bmsource.dwh.common.filemanager.FileManager;
import org.bmsource.dwh.common.filemanager.TmpFileManager;
import org.bmsource.dwh.common.io.DataRow;
import org.bmsource.dwh.common.io.reader.ExcelBeanReader;
import org.bmsource.dwh.common.io.reader.ExcelReader;
import org.bmsource.dwh.common.job.JobService;
import org.bmsource.dwh.importer.importer.MappingPreset;
import org.bmsource.dwh.importer.importer.MappingPresetRepository;
import org.bmsource.dwh.common.courier.RawFact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/import")
public class ImportController {

    private FileManager fileManager = new TmpFileManager();

    @Autowired
    private JobService jobService;

    @Autowired
    private MappingPresetRepository mappingPresetRepository;

    @PostMapping("/init")
    public String initUpload() {
        return fileManager.createTransaction();
    }

    @PostMapping("/{transactionId}")
    public List<String> fileUpload(MultipartHttpServletRequest request,
                                   @PathParam("projectId") String projectId,
                                   @PathVariable("transactionId") String transactionId) throws IOException {
        List<String> files = new ArrayList<>();
        MultipartFile multipartFile = request.getFile("file");
        assert multipartFile != null;
        String name = multipartFile.getOriginalFilename();
        try (InputStream inputStream = multipartFile.getInputStream();) {
            fileManager.addFile(transactionId, name, inputStream);
            files.add(name);
        }
        return files;
    }

    @PostMapping(value = "/{transactionId}/mapping", consumes = "application/json")
    public MappingResponse columnMapping(@PathVariable("transactionId") String transactionId,
                                         @PathParam("projectId") String projectId
    ) throws Exception {
        List<String> files = fileManager.getFiles(transactionId);
        try (InputStream stream = fileManager.getStream(transactionId, files.get(0))) {
            MappingResult columnMapping = getColumnMapping(stream);
            return MappingResponse.builder().setSourceFields(columnMapping.getHeaderRow(),
                columnMapping.getPreviewRow()).setFactModel(RawFact.class).autoSuggestionMapping().build();
        }
    }

    @PostMapping(value = "/{transactionId}/preview", consumes = "application/json")
    public List<PreviewResponse> preview(@PathVariable("transactionId") String transactionId,
                                         @PathParam("projectId") String projectId,
                                         @RequestBody PreviewRequestBody mappingParam) throws Exception {

        List<String> files = fileManager.getFiles(transactionId);
        List<PreviewResponse> rows = new ArrayList<>();
        try (
            InputStream stream1 = fileManager.getStream(transactionId, files.get(0));
            ExcelBeanReader<RawFact> reader = new ExcelBeanReader<>(stream1, RawFact.class, mappingParam.getMapping());
        ) {
            int count = 0;
            while (reader.hasNextRow() && count < 100) {
                DataRow<RawFact> item = reader.nextValidatedRow();
                rows.add(new PreviewResponse(item.getFact(), item.getErrors().getFieldErrors(), item.isValid()));
                count++;
            }
            return rows;
        }
    }

    @ResponseBody
    @PostMapping(value = "/{transactionId}/start", consumes = "application/json")
    public ResponseEntity<HttpStatus> start(@RequestHeader("x-tenant") String tenant,
                                @PathVariable("transactionId") String transactionId,
                                @PathParam("projectId") String projectId,
                                @RequestBody UploadRequestBody uploadRequestBody) {
        List<String> files = fileManager.getFiles(transactionId);
        jobService.runImport(tenant, projectId, transactionId, files, uploadRequestBody.getMapping());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/mapping-presets")
    public Iterable<MappingPreset> mappingPresets(@RequestHeader("x-tenant") String tenant,
                                                  @PathParam("projectId") String projectId) {
        return mappingPresetRepository.findAll();
    }

    @PostMapping("/mapping-presets")
    public MappingPreset saveMappingPreset(@RequestHeader("x-tenant") String tenant,
                                           @PathParam("projectId") String projectId,
                                           @RequestBody MappingPreset mappingPreset
    ) {
        return mappingPresetRepository.save(mappingPreset);
    }

    @GetMapping("/stats")
    public Map<String, Object> statistics(@RequestHeader("x-tenant") String tenant,
                                          @PathParam("projectId") String projectId) {
        return jobService.getStatistics(tenant, projectId);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/errors.zip", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void getErrorStream(@RequestHeader("x-tenant") String tenant,
                               @PathParam("projectId") String projectId,
                               HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=" + "errors.zip");
        try (InputStream is = fileManager.getStream(tenant, projectId, "errors.zip")) {
            IOUtils.copy(is, response.getOutputStream());
        }
        response.flushBuffer();
    }

    private MappingResult getColumnMapping(InputStream inputStream) throws Exception {
        ExcelReader<List<Object>> reader = new ExcelReader<>(inputStream);
        List<String> headerRow = reader.getHeader();
        List<Object> previewRow = reader.nextRow();
        reader.close();
        return new MappingResult(headerRow, previewRow);
    }
}
