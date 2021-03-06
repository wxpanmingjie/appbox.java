package appbox.store;

import appbox.logging.Log;
import appbox.model.ApplicationModel;
import appbox.model.EntityModel;
import appbox.model.entity.DataFieldModel;
import appbox.model.entity.FieldWithOrder;
import appbox.model.entity.SysIndexModel;
import appbox.utils.IdUtil;

import static appbox.model.entity.DataFieldModel.DataFieldType;

import java.util.concurrent.CompletableFuture;

/**
 * 系统存储初始化，仅用于启动集群第一节点时
 */
public final class StoreInitiator {

    public static CompletableFuture<Boolean> initAsync() {
        //TODO:考虑判断是否已初始化
        Log.debug("Start init system store...");

        return createAppAsync().thenCompose(app -> {
            try {
                //新建EntityModels
                var emploeeModel    = createEmploeeModel();
                var enterpriseModel = createEnterpriseModel();

                //开始事务保存
                return KVTransaction.beginAsync()
                        .thenCompose(txn -> ModelStore.insertModelAsync(emploeeModel, txn)
                        .thenCompose(r -> ModelStore.insertModelAsync(enterpriseModel, txn))
                        .thenApply(r -> txn.commitAsync())
                        .thenApply(r -> true));
            } catch (Exception e) {
                Log.error(e.getMessage());
                return CompletableFuture.completedFuture(false);
            }
        }).exceptionally(ex -> {
            Log.error(ex.getMessage());
            return false;
        });
    }

    private static CompletableFuture<ApplicationModel> createAppAsync() {
        var app = new ApplicationModel("appbox", "sys");

        return ModelStore.createApplicationAsync(app).thenApply(appStoreId -> {
            app.setAppStoreId(appStoreId);
            return app;
        });
    }

    private static EntityModel createEmploeeModel() throws Exception {
        var nameId     = (short) (1 << IdUtil.MEMBERID_SEQ_OFFSET);
        var maleId     = (short) (2 << IdUtil.MEMBERID_SEQ_OFFSET);
        var birthdayId = (short) (3 << IdUtil.MEMBERID_SEQ_OFFSET);
        var accountId  = (short) (4 << IdUtil.MEMBERID_SEQ_OFFSET);
        var passwordId = (short) (5 << IdUtil.MEMBERID_SEQ_OFFSET);

        var model = new EntityModel(IdUtil.SYS_EMPLOEE_MODEL_ID, "Emploee", true, false);

        //Members
        var name = new DataFieldModel(model, "Name", DataFieldType.String, false, false);
        model.addSysMember(name, nameId);
        var male = new DataFieldModel(model, "Male", DataFieldType.Bool, false, false);
        model.addSysMember(male, maleId);
        var birthday = new DataFieldModel(model, "Birthday", DataFieldType.DateTime, false, false);
        model.addSysMember(birthday, birthdayId);
        var account = new DataFieldModel(model, "Account", DataFieldType.String, true, false);
        model.addSysMember(account, accountId);
        var password = new DataFieldModel(model, "Password", DataFieldType.Binary, true, false);
        model.addSysMember(password, passwordId);

        //TODO:
        //var orgunits = new EntitySetModel(model, "OrgUnits", IdUtil.SYS_ORGUNIT_MODEL_ID, Consts.ORGUNIT_BASE_ID);
        //model.AddSysMember(orgunits, Consts.EMPLOEE_ORGUNITS_ID);

        //Indexes
        var ui_account = new SysIndexModel(model, "UI_Account", true,
                new FieldWithOrder[]{new FieldWithOrder(accountId)},
                new short[]{passwordId});
        model.sysStoreOptions().addSysIndex(model, ui_account, (byte) ((1 << IdUtil.INDEXID_UNIQUE_OFFSET) | (1 << 2)));

        return model;
    }

    private static EntityModel createEnterpriseModel() throws Exception {
        var model = new EntityModel(IdUtil.SYS_ENTERPRISE_MODEL_ID, "Enterprise", true, false);

        //Members
        var name = new DataFieldModel(model, "Name", DataFieldType.String, false, false);
        model.addSysMember(name, (short) (1 << IdUtil.MEMBERID_SEQ_OFFSET));
        var address = new DataFieldModel(model, "Address", DataFieldType.String, true, false);
        model.addSysMember(address, (short) (2 << IdUtil.MEMBERID_SEQ_OFFSET));

        return model;
    }
}
