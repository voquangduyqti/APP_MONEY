
import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //Khai báo mảng chứa các giao dịch.
    public long timeStamp;
    public int nonce;

    //Hàm tạo Block
    public Block(String previousHash ) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    //Tính toán mã băm mới dựa vào nội dung khối
    public String calculateHash() {
        String calculatedhash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedhash;
    }

    //Tăng giá trị của Nonce cho đến khi đạt được mã băm mục tiêu
    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty); //Khởi tạo chuỗi với độ khó  là "0"
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    //Thêm giao dịch vào Block
    public boolean addTransaction(Transaction transaction) {
        //xử lý giao dịch và kiểm tra xem có hợp lệ không; Nếu là khối là khối gốc thì không cần kiểm tra.
        if(transaction == null) return false;
        if((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Giao dich khong xu ly duoc!.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Giao dich da duoc them thanh cong vao khoi.");
        return true;
    }

}