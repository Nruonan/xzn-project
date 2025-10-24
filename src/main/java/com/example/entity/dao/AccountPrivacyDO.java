package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.lang.reflect.Field;
import java.util.LinkedList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_account_privacy")
public class AccountPrivacyDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    boolean phone = true;
    boolean email = true;
    boolean wx = true;
    boolean qq = true;
    boolean gender = true;
    boolean remind = false;

    public AccountPrivacyDO(Integer id) {
        this.id = id;
        this.qq = false;
        this.wx = false;
        this.phone = false;
        this.gender = false;
        this.email = false;
        this.remind = false;
    }

    public String[] hiddenFields(){
        LinkedList<String> strings = new LinkedList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                if (field.getType().equals(boolean.class) && !field.getBoolean(this)){
                    strings.add(field.getName());
                }
            }catch (Exception e){

            }
        }
        return strings.toArray(String[]::new);
    }
}
