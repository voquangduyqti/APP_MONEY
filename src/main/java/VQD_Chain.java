import java.security.Security;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
//import com.google.gson.GsonBuilder;
import java.util.Map;

public class VQD_Chain {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public static int difficulty = 3;
    public static float minimumTransaction = 0.1f;
    public static Vi_Tien Vi_TienA; //Ví A
    public static Vi_Tien Vi_TienB; //Ví B
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        //Thêm khối vào chuỗi khối


        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Thiết lập bảo mật bằng phương thức BouncyCastleProvider

        //Tạo ra các ví
        Vi_TienA = new Vi_Tien();
        Vi_TienB = new Vi_Tien();
        Vi_Tien coinbase = new Vi_Tien();

        //Khởi tạo giao dịch gốc, để chuyển 100 Coin đến ví A walletA
        genesisTransaction = new Transaction(coinbase.publicKey, Vi_TienA.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 //Gán private key (ký thủ công) vào giao dịch gốc
        genesisTransaction.transactionId = "0"; //Gán ID cho giao dịch gốc
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.reciepient, genesisTransaction.value, genesisTransaction.transactionId)); //Thêm Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //Lưu giao dịch đầu tiên vào danh sách UTXOs.

        System.out.println("Dang tao va dao khoi goc .... ");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        //Thử nghiệm
        Block block1 = new Block(genesis.hash);
        System.out.println("\nSo du cua vi A la: " + Vi_TienA.getBalance());
        System.out.println("\nGiao dich chuyen so tien la 40 tu vi A den vi B...");
        block1.addTransaction(Vi_TienA.sendFunds(Vi_TienB.publicKey, 40f));
        addBlock(block1);
        System.out.println("\nSo du  moi cua vi A la: " + Vi_TienA.getBalance());
        System.out.println("So du moi cua vi B la: " + Vi_TienB.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("\nVi A gui mot so tien nhieu hon so tien co trong vi...");
        block2.addTransaction(Vi_TienA.sendFunds(Vi_TienB.publicKey, 1000f));
        addBlock(block2);
        System.out.println("\nSo du moi cua vi A la: " + Vi_TienA.getBalance());
        System.out.println("So du moi cua vi B la:  " + Vi_TienB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("\nGiao dich chuyen so tien la 20 tu vi B den vi A...");
        block3.addTransaction(Vi_TienB.sendFunds( Vi_TienA.publicKey, 20));
        System.out.println("\nSo du moi cau vi A la: " + Vi_TienA.getBalance());
        System.out.println("So du moi cua vi B la:  " + Vi_TienB.getBalance());

        isChainValid();

    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //Tạo một danh sách hoạt động tạm thời của các giao dịch chưa được thực thi tại một trạng thái khối nhất định.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //Duyệt chuỗi khối để kiểm tra các mã băm:
        for(int i=1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //Kiểm tra, so sánh mã băm đã đăng ký với mã băm được tính toán
            if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
                System.out.println("#Ma bam khoi hien ta khong khop");
                return false;
            }
            //So sánh mã băm của khối trước với mã băm của khối trước đã được đăng ký
            if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
                System.out.println("#Ma bam khoi truoc khop");
                return false;
            }
            //Kiểm tra xem mã băm có lỗi không
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("#Khoi nay khong dao duoc do loi!");
                return false;
            }

            //Vòng lặp kiểm tra các giao dịch
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Chu ky so cua giao dich (" + t + ") khong hop le");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Cac dau vao khong khop voi dau ra giao dich (" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Cac dau vao tham chieu trong giao dich (" + t + ") bi thieu!");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Cac dau vao tham chieu trong giao dich (" + t + ") co gia tri khong hop le");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
                    System.out.println("#giao dich(" + t + ") co nguoi nhan khong dung!");
                    return false;
                }
                if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    System.out.println("#Dau ra cua giao dich (" + t + ") khong dung voi nguoi gui.");
                    return false;
                }

            }

        }
        System.out.println("Chuoi khoi hop le!");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}

