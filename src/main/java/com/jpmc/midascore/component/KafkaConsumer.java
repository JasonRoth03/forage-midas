package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaConsumer {
    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;

    public KafkaConsumer(UserRepository userRepository, TransactionRecordRepository transactionRecordRepository, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }

    @KafkaListener(id = "listener1", topics = "${general.kafka-topic}")
    public void listen(Transaction transaction) {

        long senderId =  transaction.getSenderId();
        long recipientId = transaction.getRecipientId();
        float amount = transaction.getAmount();

        UserRecord sender = userRepository.findById(senderId);
        UserRecord recipient = userRepository.findById(recipientId);
        if(sender != null && recipient != null) {
            //both the sender and receiver exist
            if(sender.getBalance() >= amount) {
                float incentiveAmount = getIncentiveAmount(transaction);
                applyTransaction(sender, recipient, amount, incentiveAmount);
            }else{
                System.out.println("Sender does not have sufficient balance");
            }
        }else{
            System.out.println("Sender or receiver does not exist");
        }
    }

    private float getIncentiveAmount(Transaction transaction) {
        String incentivesApiUrl = "http://localhost:8080/incentive";
        Incentive incentive = restTemplate.postForObject(incentivesApiUrl, transaction, Incentive.class);
        return incentive.getAmount();
    }

    private void applyTransaction(UserRecord sender, UserRecord recipient, float amount, float incentiveAmount) {
        sender.setBalance(sender.getBalance() - amount);
        userRepository.save(sender);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmount);
        userRepository.save(recipient);
        TransactionRecord transactionRecord = new TransactionRecord(sender, recipient, amount);
        transactionRecordRepository.save(transactionRecord);
    }

}
