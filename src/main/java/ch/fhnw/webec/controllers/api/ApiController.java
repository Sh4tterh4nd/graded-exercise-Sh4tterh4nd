package ch.fhnw.webec.controllers.api;

import ch.fhnw.webec.entity.Book;
import ch.fhnw.webec.entity.Tag;
import ch.fhnw.webec.repository.AuthorRepository;
import ch.fhnw.webec.repository.BookRepository;
import ch.fhnw.webec.services.AudiobookService;
import ch.fhnw.webec.services.ProgressService;
import ch.fhnw.webec.util.AudiobookUtil;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/audiobook")
public class ApiController {
    private final AudiobookService audiobookService;
    private final ProgressService progressService;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public ApiController(AudiobookService audiobookService, ProgressService progressService, AuthorRepository authorRepository, BookRepository bookRepository) {
        this.audiobookService = audiobookService;
        this.progressService = progressService;
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @PostMapping
    public void addAudiobook(@RequestParam("data") MultipartFile multipartFile) throws IOException, InvalidDataException, UnsupportedTagException {
        String newFilename = UUID.randomUUID().toString().concat(AudiobookUtil.getFileEnding(multipartFile.getOriginalFilename()));
        Path upload = Paths.get("data");
        if (Files.notExists(upload)) {
            Files.createDirectory(upload);
        }
        Path output = upload.resolve(Objects.requireNonNull(newFilename));
        OutputStream outputStream = new FileOutputStream(output.toFile());
        multipartFile.getInputStream().transferTo(outputStream);
        outputStream.flush();
        outputStream.close();

        audiobookService.addAudiobook(multipartFile.getOriginalFilename(), output);
    }

    @GetMapping(value = "{bookId}/stream")
    public ResponseEntity<FileSystemResource> streamFile(@PathVariable("bookId") Long bookId) {
        Book book = bookRepository.findBookById(bookId);
        if (book == null) return ResponseEntity.notFound().build();

        Path bookPath = Paths.get("data", book.getFilename());
        return ResponseEntity.ok().body(new FileSystemResource(bookPath));
    }


    @PutMapping("{bookId}/")
    public ResponseEntity updateBook(@PathVariable("bookId") Long bookId, @RequestBody Book book) {
        if (book.getId().equals(bookId)) {
            audiobookService.updateAudiobook(book);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }

    }

    @DeleteMapping("{bookId}/")
    public void deleteBook(@PathVariable("bookId") Long bookId){
        audiobookService.deleteAudiobook(bookId);
    }
}