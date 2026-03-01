package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.RecurringTransactionDTO;
import in.utkarshsingh.money.manager.dto.request.RecurringTransactionRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.entity.RecurringTransactionEntity;
import in.utkarshsingh.money.manager.enums.TransactionFrequency;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.RecurringTransactionMapper;
import in.utkarshsingh.money.manager.repository.CategoryRepository;
import in.utkarshsingh.money.manager.repository.RecurringTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepo;
    private final CategoryRepository categoryRepository;
    private final UserResolverService userResolverService;
    private final RecurringTransactionMapper mapper;

    @Transactional
    public RecurringTransactionDTO create(RecurringTransactionRequest request) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        RecurringTransactionEntity entity = mapper.toEntity(request, profile, category);
        entity = recurringRepo.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<RecurringTransactionDTO> getAll() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        return recurringRepo.findByProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public RecurringTransactionDTO update(Long id, RecurringTransactionRequest request) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        RecurringTransactionEntity entity = recurringRepo.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction", id));
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setAmount(request.getAmount());
        entity.setType(request.getType());
        entity.setFrequency(request.getFrequency());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setCategory(category);

        entity = recurringRepo.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public RecurringTransactionDTO toggleActive(Long id) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        RecurringTransactionEntity entity = recurringRepo.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction", id));
        entity.setIsActive(!entity.getIsActive());
        entity = recurringRepo.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        RecurringTransactionEntity entity = recurringRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction", id));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this recurring transaction");
        }
        recurringRepo.delete(entity);
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processRecurringTransactions() {
        log.info("Processing recurring transactions...");
        LocalDate today = LocalDate.now();
        List<RecurringTransactionEntity> due = recurringRepo.findByIsActiveTrueAndNextExecutionDateLessThanEqual(today);

        for (RecurringTransactionEntity rt : due) {
            if (rt.getEndDate() != null && today.isAfter(rt.getEndDate())) {
                rt.setIsActive(false);
                recurringRepo.save(rt);
                continue;
            }
            rt.setNextExecutionDate(calculateNextDate(rt.getNextExecutionDate(), rt.getFrequency()));
            recurringRepo.save(rt);
        }
        log.info("Processed {} recurring transactions", due.size());
    }

    private LocalDate calculateNextDate(LocalDate current, TransactionFrequency frequency) {
        return switch (frequency) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case MONTHLY -> current.plusMonths(1);
            case YEARLY -> current.plusYears(1);
        };
    }
}
