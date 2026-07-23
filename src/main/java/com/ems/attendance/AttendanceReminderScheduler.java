package com.ems.attendance;

import com.ems.model.UserInfo;
import com.ems.notification.NotificationService;
import com.ems.notification.NotificationType;
import com.ems.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AttendanceReminderScheduler {

    private final AttendanceRepository attendanceRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 20 * * *")
    public void remindMissingPunchOuts() {
        for (AttendanceRecord record : attendanceRepository.findByDate(LocalDate.now())) {
            if (record.getCheckIn() == null || record.getCheckOut() != null) {
                continue;
            }
            UserInfo recipient = record.getEmployee().getUser();
            if (recipient == null) {
                continue;
            }

            NotificationRequest notification = new NotificationRequest();
            notification.setTitle("Forgot to punch out?");
            notification.setMessage(
                    "You checked in at " + record.getCheckIn() + " but haven't punched out yet today.");
            notification.setType(NotificationType.WARNING);
            notification.setRecipientId(recipient.getId());
            notificationService.create(notification);
        }
    }
}
