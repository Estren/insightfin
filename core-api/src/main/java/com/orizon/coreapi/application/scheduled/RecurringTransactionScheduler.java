package com.orizon.coreapi.application.scheduled;

import com.orizon.coreapi.application.service.RecurringTransactionService;
import com.orizon.coreapi.domain.port.out.RecurringTransactionRepository;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;

@ApplicationScoped
public class RecurringTransactionScheduler {

    private static final Logger LOG = Logger.getLogger(RecurringTransactionScheduler.class);

    @Inject
    RecurringTransactionRepository recurringRepository;

    @Inject
    RecurringTransactionService recurringService;

    @Scheduled(cron = "0 0 5 * * ?")
    public void generateDueTransactions() {
        LocalDate today = LocalDate.now();
        var due = QuarkusTransaction.requiringNew().call(() -> recurringRepository.findDueByDate(today));
        if (due.isEmpty()) return;

        LOG.infof("Generating recurring transactions for %d due recurrences", due.size());
        int generated = 0;
        for (var recurring : due) {
            try {
                QuarkusTransaction.requiringNew().run(() -> recurringService.generateDue(recurring, today));
                generated++;
            } catch (Exception e) {
                LOG.errorf(e, "Failed to generate transactions for recurring %s", recurring.getId());
            }
        }
        LOG.infof("Recurring scheduler done: %d/%d processed", generated, due.size());
    }
}
