package com.inpro.asBoard.storage;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.File;
import java.util.Collection;

@Component
public class TransactionalFileOps {

    //========================================================================
    // 1. 롤백 시 임시파일 정리 등록
    // 2. URL : (Component) TransactionSynchronization
    // 3. Param : createdFiles(required, Collection<File>)
    // 4. 설명 : 현재 트랜잭션이 ROLLBACK 되면 생성했던 파일들을 물리 삭제하도록 콜백 등록
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    public void registerRollbackCleanup(Collection<File> createdFiles) {
        if (createdFiles == null || createdFiles.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    for (File f : createdFiles) try { if (f.exists()) f.delete(); } catch (Exception ignore) {}
                }
            }
        });
    }

    //========================================================================
    // 1. 커밋 후 파일 삭제 등록
    // 2. URL : (Component) TransactionSynchronization
    // 3. Param : toDelete(required, Collection<File>)
    // 4. 설명 : 현재 트랜잭션 COMMIT 성공 후 대상 파일들을 물리 삭제하도록 콜백 등록
    // 5. 작성 : wjlee(25.09.01)
    // 6. 수정 :
    //========================================================================
    public void registerDeleteAfterCommit(Collection<File> toDelete) {
        if (toDelete == null || toDelete.isEmpty()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                for (File f : toDelete) try { if (f.exists()) f.delete(); } catch (Exception ignore) {}
            }
        });
    }
}