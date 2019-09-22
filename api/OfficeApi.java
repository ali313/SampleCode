package ir.nabaksoft.office.api;

import java.util.List;
import ir.nabaksoft.office.model.Folder;
import ir.nabaksoft.office.model.BaseApiModel;
import ir.nabaksoft.office.model.Letter;
import ir.nabaksoft.office.model.ListModel;
import ir.nabaksoft.office.model.Message;
import ir.nabaksoft.office.model.Person;
import ir.nabaksoft.office.model.RolePerson;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Ali on 5/17/2019.
 */

public interface OfficeApi
{
    @FormUrlEncoded
    @POST("Api/letter/List/Letter.ashx")
    Call<ListModel<Letter>> RecipientLetters(@Field("start") int start,
                                        @Field("limit") int limit,
                                        @Field("folderId") int folderId,
                                        @Field("letterStateType") @Constants.LetterState int letterStateType,
                                        @Field("letterListType") @Constants.LetterType int letterListType,
                                        @Field("isDraft") boolean isDraft);

    @FormUrlEncoded
    @POST("Api/letter/List/LetterList.ashx")
    Call<ListModel<Letter>> LetterList(@Field("start") int start,
                                        @Field("limit") int limit,
                                        @Field("folderId") int folderId,
                                        @Field("letterStateType") @Constants.LetterState int letterStateType,
                                        @Field("letterListType") @Constants.LetterType int letterListType,
                                        @Field("isDraft") boolean isDraft);


    @FormUrlEncoded
    @POST("Api/letter/List/MessageList.ashx")
    Call<ListModel<Message>> MessageList(@Field("start") int start,
                                         @Field("limit") int limit,
                                         @Field("messageListType") @Constants.MessageType int MessageListType);
    @FormUrlEncoded
    @POST("Api/Letter/ViewMessage.ashx")
    Call<Message> getMessage(@Field("RecId") long recId, @Field("type") int type);

    @FormUrlEncoded
    @POST("Api/Person/Login.ashx")
    Call<Person> login(@Field("username") String username,
                       @Field("password") String password);

    @FormUrlEncoded
    @POST("Api/Letter/ViewLetter.ashx")
    Call<Letter> getLetter(@Field("RecId") long RecId,
                       @Field("type") String type);

    @FormUrlEncoded
    @POST("Api/Letter/List/Folder.ashx")
    Call<ListModel<Folder>> getFolders(@Field("letterStateType") @Constants.LetterState int letterStateType,
                                  @Field("letterListType") @Constants.LetterType int letterListType,
                                  @Field("isDraft") boolean isDraft);

    @FormUrlEncoded
    @POST("Api/Letter/LetterCommand.ashx")
    Call<BaseApiModel> commandLetter(@Field("RecId") long RecId,
                                     @Field("type") String type,
                                     @Field("command") String command);

    @FormUrlEncoded
    @POST("Api/Person/SearchRole.ashx")
    Call<ListModel<RolePerson>> searchRole(@Field("searchKey") String searchKey);

    @FormUrlEncoded
    @POST("Api/Letter/LetterCommand.ashx")
    Call<BaseApiModel> AddLetterRefrence(@Field("RecId") long RecId,
                                         @Field("type") String type,
                                         @Field("command") String command,
                                         @Field("newRecipientRoleId") long newRecipientRoleId,
                                         @Field("refrenceId") int refrenceId,
                                         @Field("body") String body);

    @FormUrlEncoded
    @POST("Api/Person/ErrorLog.ashx")
    Call<BaseApiModel> errorLog(@Field("error") String error);
}
