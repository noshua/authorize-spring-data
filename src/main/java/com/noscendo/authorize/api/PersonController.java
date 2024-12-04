package com.noscendo.authorize.api;

import com.noscendo.authorize.mapper.PersonMapper;
import com.noscendo.authorize.model.api.PersonRequest;
import com.noscendo.authorize.model.api.PersonResponse;
import com.noscendo.authorize.model.api.PersonSearch;
import com.noscendo.authorize.model.dao.Person;
import com.noscendo.authorize.repo.PersonRepository;
import com.noscendo.authorize.service.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@RequestMapping("/person")
@RequiredArgsConstructor
@RestController
@Slf4j
@Transactional
public class PersonController {

    private final PersonMapper personMapper;
    private final PersonService personService;
    private final PersonRepository personRepository;

    @PostMapping
    public ResponseEntity<PersonResponse> post(@RequestBody PersonRequest request) {
//        Person created = personService.create(request);
        Person created = personMapper.create(request);
        Person saved = personRepository.save(created);
        return ResponseEntity.ok(personMapper.retrieve(saved));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<PersonResponse> get(@PathVariable Long id) {
//        Person read = personService.read(id);
        Person read = personService.findAuthorized(id);
        return ResponseEntity.ok(personMapper.retrieve(read));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PersonResponse> patch(@PathVariable Long id, @RequestBody PersonRequest request) {
//        Person read = personService.read(id);
//        Person patched = personService.patch(id, request);
        Person existing = personService.findAuthorized(id);
        Person patched = personMapper.patch(request, existing);
        Person saved = personRepository.save(patched);
        return ResponseEntity.ok(personMapper.retrieve(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponse> put(@PathVariable Long id, @RequestBody PersonRequest request) {
//        Person updated = personService.update(id, request);
//        Person existing = personRepository.findById(id).orElseThrow();
        Person existing = personService.findAuthorized(id);
        Person patched = personMapper.update(request, existing);
        Person saved = personRepository.save(patched);
        return ResponseEntity.ok(personMapper.retrieve(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Person existing = personRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        personRepository.deleteById(existing.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("search")
    public ResponseEntity<List<PersonResponse>> search(@RequestBody PersonSearch search) {
        Specification<Person> spec = Specification.where((root, query, cb) -> cb.equal(root.get("name"), search.getName()));

        List<Sort.Order> orders = Stream.ofNullable(search.getOrders())
                .flatMap(Collection::stream)
                .map(orderBy -> new Sort.Order(orderBy.getDirection(), orderBy.getProperty()))
                .toList();

        PageRequest pageable = PageRequest.of(search.getPageNumber(), search.getPageSize(), Sort.by(orders));
//        Page<Person> page = personRepository.findAll(spec, pageable);
        Page<Person> page = personService.findAllAuthorized(spec, pageable);

        return ResponseEntity.ok(page.get().map(personMapper::retrieve).toList());
    }
}
