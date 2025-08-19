public class Email {
    String id;
    String subject;
    String toRecipients;
    String from;
    String receivedDateTime;
    String bodyPreview;

    public Email(String id, String subject, String from,String toRecipients,  String receivedDateTime, String bodyPreview) {
        this.id = id;
        this.subject = subject;
        this.from = from;
        this.toRecipients = toRecipients;
        this.receivedDateTime = receivedDateTime;
        this.bodyPreview = bodyPreview;
    }
}