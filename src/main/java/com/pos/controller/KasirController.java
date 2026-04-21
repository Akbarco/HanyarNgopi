package com.pos.controller;

import com.pos.dao.MenuDAO;
import com.pos.dao.TransaksiDAO;
import com.pos.model.Menu;
import com.pos.model.Transaksi;
import com.pos.model.TransaksiDetail;
import com.pos.service.AuthService;
import com.pos.service.KasirService;
import com.pos.util.AlertUtil;
import com.pos.util.ToastUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class KasirController implements Initializable {

    @FXML private VBox containerRiwayat;
    @FXML private ScrollPane scrollRiwayat;

    private final KasirService kasirService = new KasirService();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final MenuDAO menuDAO = new MenuDAO();
    private final Locale localeId = new Locale("id", "ID");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadRiwayat();
    }

    private void loadRiwayat() {
        if (containerRiwayat == null) {
            return;
        }

        containerRiwayat.getChildren().clear();
        List<Transaksi> list = transaksiDAO.findAll();

        if (list.isEmpty()) {
            Label empty = new Label("Belum ada transaksi");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
            containerRiwayat.getChildren().add(empty);
            return;
        }

        for (Transaksi transaksi : list) {
            List<TransaksiDetail> details =
                    transaksiDAO.findDetailByTransaksiId(transaksi.getIdTransaksi());
            containerRiwayat.getChildren().add(buildTransaksiCard(transaksi, details));
        }
    }

    private VBox buildTransaksiCard(Transaksi transaksi, List<TransaksiDetail> details) {
        VBox card = new VBox(12);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: #F8FAFF;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #D7E3FF;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1;"
        );

        String metodeRaw = details.isEmpty() ? "-" : details.get(0).getMetodePembayaran();
        String metode = formatMetodeLabel(metodeRaw);
        String tanggal = transaksi.getTanggal().format(
                DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'pukul' HH.mm",
                        new Locale("id", "ID"))
        );

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox leftInfo = new VBox(6);
        Label lblTanggal = new Label(tanggal);
        lblTanggal.setStyle("-fx-font-size: 13; -fx-text-fill: #475569;");

        Label badgeMetode = new Label(metode);
        badgeMetode.setStyle(
                "-fx-background-color: " + ("QRIS".equals(metode) ? "#FEF3C7" : "#EEF2FF") + ";" +
                        "-fx-text-fill: " + ("QRIS".equals(metode) ? "#B45309" : "#4F46E5") + ";" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: 4 14 4 14;" +
                        "-fx-font-size: 11;" +
                        "-fx-font-weight: bold;"
        );
        leftInfo.getChildren().addAll(lblTanggal, badgeMetode);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox rightInfo = new VBox(2);
        rightInfo.setAlignment(Pos.CENTER_RIGHT);
        Label lblTotalLabel = new Label("Total Pembayaran");
        lblTotalLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");
        Label lblTotal = new Label(formatCurrency(transaksi.getTotal()));
        lblTotal.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #16A34A;");
        rightInfo.getChildren().addAll(lblTotalLabel, lblTotal);

        headerRow.getChildren().addAll(leftInfo, spacer, rightInfo);

        Region divider = new Region();
        divider.setMinHeight(1);
        divider.setPrefHeight(1);
        divider.setMaxHeight(1);
        divider.setStyle("-fx-background-color: #E5E7EB;");

        Label lblItems = new Label("Item Pesanan:");
        lblItems.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #374151;");

        FlowPane itemsGrid = new FlowPane();
        itemsGrid.setHgap(12);
        itemsGrid.setVgap(12);
        itemsGrid.setPrefWrapLength(640);

        for (TransaksiDetail detail : details) {
            HBox itemCard = new HBox(16);
            itemCard.setPadding(new Insets(12, 16, 12, 16));
            itemCard.setAlignment(Pos.CENTER_LEFT);
            itemCard.setMinWidth(300);
            itemCard.setPrefWidth(320);
            itemCard.setMaxWidth(320);
            itemCard.getStyleClass().add("transaction-item-card");

            VBox itemInfo = new VBox(2);
            itemInfo.setMinWidth(0);
            HBox.setHgrow(itemInfo, Priority.ALWAYS);
            Label lblNama = new Label(detail.getNamaMenu());
            lblNama.getStyleClass().add("transaction-item-name");
            lblNama.setWrapText(true);
            lblNama.setMaxWidth(Double.MAX_VALUE);
            Label lblQty = new Label("Qty: " + detail.getQty());
            lblQty.getStyleClass().add("transaction-item-meta");
            itemInfo.getChildren().addAll(lblNama, lblQty);

            Region itemSpacer = new Region();
            HBox.setHgrow(itemSpacer, Priority.ALWAYS);

            Label lblSubtotal = new Label(formatCurrency(detail.getSubtotal()));
            lblSubtotal.getStyleClass().add("transaction-item-subtotal");
            lblSubtotal.setMinWidth(110);

            itemCard.getChildren().addAll(itemInfo, itemSpacer, lblSubtotal);
            itemsGrid.getChildren().add(itemCard);
        }

        card.getChildren().addAll(headerRow, divider, lblItems, itemsGrid);
        return card;
    }

    @FXML
    public void handleTransaksiBaru() {
        showTransaksiDialog();
    }

    private void showTransaksiDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("Transaksi Baru");

        if (containerRiwayat != null && containerRiwayat.getScene() != null) {
            dialog.initOwner(containerRiwayat.getScene().getWindow());
        }

        ObservableList<TransaksiDetail> keranjang = FXCollections.observableArrayList();

        VBox root = new VBox(24);
        root.setPadding(new Insets(28, 30, 30, 30));
        root.getStyleClass().add("dialog-root");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(12);

        Label lblTitle = new Label("Transaksi Baru");
        lblTitle.getStyleClass().add("dialog-title");

        header.getChildren().add(lblTitle);

        HBox inputRow = new HBox(18);
        inputRow.setAlignment(Pos.BOTTOM_LEFT);

        VBox menuBox = new VBox(8);
        HBox.setHgrow(menuBox, Priority.ALWAYS);
        Label lblPilihMenu = fieldLabel("Pilih Menu");

        ComboBox<Menu> cmbMenu = new ComboBox<>();
        cmbMenu.setItems(FXCollections.observableArrayList(menuDAO.findByActive(true)));
        cmbMenu.setPromptText("Pilih menu");
        cmbMenu.setMaxWidth(Double.MAX_VALUE);
        cmbMenu.getStyleClass().add("combo-input");
        cmbMenu.setCellFactory(lv -> createMenuCell());
        cmbMenu.setButtonCell(createMenuCell());
        menuBox.getChildren().addAll(lblPilihMenu, cmbMenu);

        VBox qtyBox = new VBox(8);
        qtyBox.setPrefWidth(120);
        Label lblJumlah = fieldLabel("Jumlah");

        TextField txtQty = new TextField("1");
        txtQty.getStyleClass().add("text-input");
        qtyBox.getChildren().addAll(lblJumlah, txtQty);

        Button btnTambah = new Button("Tambah");
        btnTambah.getStyleClass().add("primary-button");
        btnTambah.setPrefWidth(130);
        btnTambah.setPrefHeight(44);

        inputRow.getChildren().addAll(menuBox, qtyBox, btnTambah);

        VBox keranjangSection = new VBox(10);
        Label lblKeranjang = fieldLabel("Keranjang");

        VBox keranjangContainer = new VBox(10);
        keranjangContainer.setFillWidth(true);

        ScrollPane scrollKeranjang = new ScrollPane(keranjangContainer);
        scrollKeranjang.setFitToWidth(true);
        scrollKeranjang.setPrefHeight(156);
        scrollKeranjang.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;" +
                        "-fx-border-color: transparent;"
        );

        keranjangSection.getChildren().addAll(lblKeranjang, scrollKeranjang);

        HBox bottomRow = new HBox(20);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        VBox metodeBox = new VBox(8);
        metodeBox.setPrefWidth(240);
        Label lblMetode = fieldLabel("Metode Pembayaran");

        ComboBox<String> cmbMetode = new ComboBox<>();
        cmbMetode.setItems(FXCollections.observableArrayList("Tunai", "QRIS"));
        cmbMetode.setValue("Tunai");
        cmbMetode.setMaxWidth(Double.MAX_VALUE);
        cmbMetode.getStyleClass().add("combo-input");
        metodeBox.getChildren().addAll(lblMetode, cmbMetode);

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        VBox totalBox = new VBox(4);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblTotalTitle = new Label("Total Pembayaran");
        lblTotalTitle.getStyleClass().add("form-caption");
        Label lblTotal = new Label("Rp 0");
        lblTotal.getStyleClass().add("total-value");
        totalBox.getChildren().addAll(lblTotalTitle, lblTotal);

        bottomRow.getChildren().addAll(metodeBox, bottomSpacer, totalBox);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnBatal = new Button("Batal");
        btnBatal.getStyleClass().add("secondary-button");
        btnBatal.setOnAction(e -> dialog.close());

        Button btnSimpan = new Button("Simpan Transaksi");
        btnSimpan.getStyleClass().add("primary-button");
        btnSimpan.setDisable(true);

        footer.getChildren().addAll(btnBatal, btnSimpan);

        root.getChildren().addAll(header, inputRow, keranjangSection, bottomRow, footer);

        AtomicReference<Runnable> refreshRef = new AtomicReference<>();
        refreshRef.set(() -> renderKeranjang(keranjang, keranjangContainer, btnSimpan, lblTotal, refreshRef));

        btnTambah.setOnAction(e -> {
            Menu menu = cmbMenu.getValue();
            String qtyStr = txtQty.getText().trim();

            if (menu == null || qtyStr.isEmpty()) {
                AlertUtil.showError("Validasi", "Pilih menu dan isi jumlah dulu.");
                return;
            }

            int qty;
            try {
                qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Validasi", "Jumlah harus angka positif.");
                return;
            }

            int availableStock = kasirService.getAvailableStock(menu.getIdMenu());
            int currentQty = getCartQty(keranjang, menu.getIdMenu());
            if (availableStock <= 0) {
                AlertUtil.showError("Stok Tidak Tersedia",
                        "Stok untuk \"" + menu.getNamaMenu() + "\" belum tersedia.");
                return;
            }
            if (currentQty + qty > availableStock) {
                AlertUtil.showError("Stok Tidak Cukup",
                        "Sisa stok \"" + menu.getNamaMenu() + "\" hanya " + availableStock + ".");
                return;
            }

            mergeCartItem(keranjang, menu, qty);
            refreshRef.get().run();
            cmbMenu.setValue(null);
            txtQty.setText("1");
            txtQty.requestFocus();
        });

        btnSimpan.setOnAction(e -> {
            if (keranjang.isEmpty()) {
                AlertUtil.showError("Error", "Keranjang kosong!");
                return;
            }

            if (AuthService.getLoggedInUser() == null) {
                AlertUtil.showError("Session", "Sesi login tidak ditemukan.");
                return;
            }

            String validation = kasirService.validateStockAvailability(new ArrayList<>(keranjang));
            if (validation != null) {
                AlertUtil.showError("Stok Tidak Cukup", validation);
                return;
            }

            double total = kasirService.hitungTotal(new ArrayList<>(keranjang));
            Transaksi transaksi = new Transaksi();
            transaksi.setIdUser(AuthService.getLoggedInUser().getIdUser());
            transaksi.setTotal(total);

            String metodeDb = "QRIS".equals(cmbMetode.getValue()) ? "qris" : "cash";

            try {
                boolean success = kasirService.simpanTransaksi(
                        transaksi, new ArrayList<>(keranjang), metodeDb
                );

                if (success) {
                    dialog.close();
                    loadRiwayat();
                    ToastUtil.showSuccess(dialog,
                            "Transaksi berhasil. Total: " + formatCurrency(total));
                } else {
                    AlertUtil.showError("Error", "Gagal menyimpan transaksi!");
                }
            } catch (IllegalStateException ex) {
                AlertUtil.showError("Stok Tidak Cukup", ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtil.showError("Error", "Gagal menyimpan transaksi!");
            }
        });

        refreshRef.get().run();

        Scene scene = new Scene(root, 680, 520);
        String menuCss = getClass().getResource("/com/pos/view/css/menu.css").toExternalForm();
        root.getStylesheets().add(menuCss);
        scene.getStylesheets().add(menuCss);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private ListCell<Menu> createMenuCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatMenuOption(item));
                }
            }
        };
    }

    private void renderKeranjang(ObservableList<TransaksiDetail> keranjang,
                                 VBox keranjangContainer,
                                 Button btnSimpan,
                                 Label lblTotal,
                                 AtomicReference<Runnable> refreshRef) {
        keranjangContainer.getChildren().clear();

        if (keranjang.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPrefHeight(90);
            emptyBox.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                            "-fx-border-color: #CBD5E1;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-style: dashed;"
            );

            Label emptyLabel = new Label("Keranjang kosong");
            emptyLabel.getStyleClass().add("placeholder-text");
            emptyBox.getChildren().add(emptyLabel);
            keranjangContainer.getChildren().add(emptyBox);

            btnSimpan.setDisable(true);
        } else {
            for (TransaksiDetail item : keranjang) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 14, 12, 14));
                row.getStyleClass().add("transaction-item-card");

                VBox info = new VBox(4);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label nama = new Label(item.getNamaMenu());
                nama.getStyleClass().add("transaction-item-name");

                Label detail = new Label("Qty " + item.getQty() + " x " + formatCurrency(item.getHarga()));
                detail.getStyleClass().add("transaction-item-meta");
                info.getChildren().addAll(nama, detail);

                Label subtotal = new Label(formatCurrency(item.getSubtotal()));
                subtotal.getStyleClass().add("transaction-item-subtotal");
                subtotal.setMinWidth(110);

                Button btnHapus = new Button("Hapus");
                btnHapus.getStyleClass().add("transaction-item-remove");
                btnHapus.setOnAction(e -> {
                    keranjang.remove(item);
                    refreshRef.get().run();
                });

                row.getChildren().addAll(info, subtotal, btnHapus);
                keranjangContainer.getChildren().add(row);
            }

            btnSimpan.setDisable(false);
        }

        lblTotal.setText(formatCurrency(
                keranjang.stream().mapToDouble(TransaksiDetail::getSubtotal).sum()
        ));
    }

    private void mergeCartItem(ObservableList<TransaksiDetail> keranjang, Menu menu, int qty) {
        for (TransaksiDetail item : keranjang) {
            if (item.getIdMenu() == menu.getIdMenu()) {
                item.setQty(item.getQty() + qty);
                item.setSubtotal(item.getHarga() * item.getQty());
                return;
            }
        }

        TransaksiDetail detail = new TransaksiDetail();
        detail.setIdMenu(menu.getIdMenu());
        detail.setQty(qty);
        detail.setHarga(menu.getHarga());
        detail.setSubtotal(menu.getHarga() * qty);
        detail.setNamaMenu(menu.getNamaMenu());
        detail.setMetodePembayaran("cash");
        keranjang.add(detail);
    }

    private int getCartQty(List<TransaksiDetail> keranjang, int idMenu) {
        int total = 0;
        for (TransaksiDetail detail : keranjang) {
            if (detail.getIdMenu() == idMenu) {
                total += detail.getQty();
            }
        }
        return total;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private String inputStyle() {
        return "-fx-background-color: #F3F4F6;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: transparent;" +
                "-fx-border-radius: 10;" +
                "-fx-pref-height: 44;" +
                "-fx-font-size: 13;" +
                "-fx-padding: 0 14 0 14;";
    }

    private String selectStyle() {
        return "-fx-background-color: #F3F4F6;" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: transparent;" +
                "-fx-border-radius: 10;" +
                "-fx-pref-height: 44;" +
                "-fx-font-size: 13;" +
                "-fx-padding: 0 14 0 14;";
    }

    private String primaryActionStyle() {
        return "-fx-background-color: #111827;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: white;" +
                "-fx-text-fill: #374151;" +
                "-fx-border-color: #D1D5DB;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-pref-height: 44;" +
                "-fx-padding: 0 18 0 18;";
    }

    private String enabledButtonStyle() {
        return "-fx-background-color: #111827;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-cursor: hand;" +
                "-fx-pref-height: 44;" +
                "-fx-padding: 0 22 0 22;";
    }

    private String disabledButtonStyle() {
        return "-fx-background-color: #A1A1AA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-pref-height: 44;" +
                "-fx-padding: 0 22 0 22;";
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(localeId);
        formatter.setMaximumFractionDigits(0);
        formatter.setMinimumFractionDigits(0);
        return "Rp " + formatter.format(amount);
    }

    private String formatMenuOption(Menu menu) {
        return menu.getNamaMenu() + " - " + formatCurrency(menu.getHarga());
    }

    private String formatMetodeLabel(String metode) {
        if (metode == null) {
            return "-";
        }
        return switch (metode.toLowerCase()) {
            case "cash" -> "Tunai";
            case "qris" -> "QRIS";
            default -> metode;
        };
    }
}
