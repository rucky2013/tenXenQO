package com.tenXen.client.controller;

import com.tenXen.client.common.ConnectContainer;
import com.tenXen.client.controller.component.CharItemControl;
import com.tenXen.client.util.LayoutLoader;
import com.tenXen.common.constant.Constants;
import com.tenXen.common.util.StringUtil;
import com.tenXen.core.domain.User;
import com.tenXen.core.model.MessageModel;
import com.tenXen.core.model.UserModel;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wt on 2016/10/28.
 */
public class CharControl {

    private CharControl() {
    }

    private static CharControl instance = new CharControl();

    public static CharControl getInstance() {
        return instance;
    }

    @FXML
    private TextArea sendBox;
    @FXML
    private VBox charBox;
    @FXML
    private ListView userBox;
    @FXML
    private Button send;
    @FXML
    private ScrollPane userScroll;
    @FXML
    private ScrollPane charScroll;

    private Stage charStage;

    public void initCharLayout() {
        try {
            this.charStage = new Stage();
            Platform.setImplicitExit(false);
            createTrayIcon(charStage);
            FXMLLoader loader = LayoutLoader.load(LayoutLoader.CHAR);
            loader.setController(CharControl.getInstance());
            Parent charLayout = loader.load();
            charStage.getIcons().add(new javafx.scene.image.Image(LayoutLoader.STAG_IMAGE));
            charStage.setTitle("tenXenQO");
            charStage.initModality(Modality.NONE);
            charStage.setScene(new Scene(charLayout));
            charStage.initStyle(StageStyle.UNIFIED);
            charStage.setResizable(false);
            charStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        this.charBox.heightProperty().addListener((observable, oldvalue, newValue) ->
                charScroll.setVvalue((Double) newValue)
        );
        this.sendBox.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.ENTER) {
                doSend();
            }
        });
    }

    @FXML
    private void doSend() {
        String sms = this.sendBox.getText();
        if (!StringUtil.isBlank(sms)) {
            MessageModel model = new MessageModel();
            model.setContent(sms);
            User u = ConnectContainer.SELF;
            if (u != null) {
                model.setUser(u.getId());
                model.setTouser(0);
                model.setCreateTime(new Date());
                model.setUserName(u.getUserName());
                model.setNickName(u.getNickname());
            }
            ConnectContainer.CHANNEL.writeAndFlush(model);
        }
        this.sendBox.setText("");
    }

    public void receiveMessage(MessageModel model) {
        try {
            CharItemControl charItemControl = new CharItemControl(model);
            FXMLLoader loader = LayoutLoader.load(LayoutLoader.CHAR_ITEM);
            loader.setController(charItemControl);
            Pane charItem = loader.load();

            this.charBox.getChildren().add(charItem);
            if (this.charBox.getChildren().size() > 50) {
                this.charBox.getChildren().remove(0, 20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateOnlineUser(UserModel model) {
        List<User> list = model.getUserList();
        List<String> userNames = new ArrayList();
        for (User user : list) {
            String userName = user.getUserName();
            String name = user.getNickname();
            if (!StringUtil.isBlank(name)) {
                userNames.add(name);
            } else {
                userNames.add(userName);
            }
        }
        ObservableList<String> users = FXCollections.observableArrayList(userNames);
        this.userBox.setItems(users);
    }

    private void createTrayIcon(Stage stage) {
        try {
            Toolkit.getDefaultToolkit();
            if (!SystemTray.isSupported()) {
                Platform.exit();
                return;
            }
            stage.setOnCloseRequest(t -> hide());
            SystemTray tray = SystemTray.getSystemTray();
            Image image = ImageIO.read(LayoutLoader.TRAY_IMAGE);
            TrayIcon trayIcon = new TrayIcon(image);
            trayIcon.addActionListener(event -> show());
            MenuItem openItem = new MenuItem("show");
            openItem.addActionListener(event -> show());
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(event -> exit());
            final PopupMenu popup = new PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            tray.add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void show() {
        Platform.runLater(() -> {
            if (charStage != null) {
                charStage.show();
                charStage.toFront();
            }
        });
    }

    private void hide() {
        Platform.runLater(() -> {
            if (SystemTray.isSupported()) {
                charStage.hide();
            } else {
                System.exit(0);
            }
        });
    }

    private void exit() {
        Platform.runLater(() -> {
            System.out.print("监听到窗口关闭");
            try {
                UserModel model = new UserModel();
                model.setHandlerCode(Constants.LOGOUT_CODE);
                model.setSelf(ConnectContainer.SELF);
                model.setResultCode(Constants.RESULT_FAIL);
                ConnectContainer.CHANNEL.writeAndFlush(model);
                ConnectContainer.USER_GROUP.shutdownGracefully();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        });
    }
}
