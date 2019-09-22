package ir.nabaksoft.office.api;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import ir.amp.tools.utils.PreferencesUtils;
import ir.nabaksoft.office.model.Person;
import ir.nabaksoft.office.tools.DeviceInfo;

/**
 * Created by Ali on 5/18/2019.
 */

public class DefaultPost
{
    public static Map<String, String> get(Context context)
    {
        Person person = Person.get(context);
        if(person.selectedRoleId==0 && person.roles!=null)
        {
                person.selectedRoleId = person.roles.get(0).Id;
            //person.selectedRoleId=2;
        }
        Map<String, String> data = new HashMap<>();
        data.put("__deviceInfo", DeviceInfo.getAllBase64(context));
        data.put("PersonId", person.PersonID + "");
        data.put("RoleId", person.selectedRoleId + "");
        data.put("token", person.token + "");
        return data;
    }
}
