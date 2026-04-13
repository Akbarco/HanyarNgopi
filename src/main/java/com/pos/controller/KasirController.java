package com.pos.controller;

import com.pos.dao.MenuDAO;
import com.pos.dao.TransaksiDAO;
import com.pos.model.Menu;
import com.pos.model.Transaksi;
import com.pos.model.TransaksiDetail;
import com.pos.service.AuthService;
import com.pos.service.KasirService;
import com.pos.util.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
        containerRiwayat.getChildren().clear();
        List<Transaksi> list = transaksiDAO.findAll();

        if (list.isEmpty()) {
            Label empty = new Label("Belum ada transaksi");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
            containerRiwayat.getChildren().add(empty);
            return;
        }

        for (Transaksi t : list) {
            List<TransaksiDetail> details = transaksiDAO.findDetailByTransaksiId(t.getIdTransaksi());
            containerRiwayat.getChildren().add(buildTransaksiCard(t, details));
        }
    }

    private VBox buildTransaksiCard(Transaksi t, List<TransaksiDetail> details) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: #F8F9FF;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #E0E7FF;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;"
        );

        String metodeRaw = details.isEmpty() ? "-" : details.get(0).getMetodePembayaran();
        String metode = formatMetodeLabel(metodeRaw);
        String tanggal = t.getTanggal().format(
                DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy 'pukul' HH.mm",
                        new Locale("id", "ID"))
        );

        HBox headerRow = new HBox();
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox leftInfo = new VBox(6);
        Label lblTanggal = new Label(tanggal);
        lblTanggal.setStyle("-fx-font-size: 13; -fx-text-fill: #6B7280;");

        Label badgeMetode = new Label(metode);
        badgeMetode.setStyle(
                "-fx-background-color: " + ("QRIS".equals(metode) ? "#FEF3C7" : "#EEF2FF") + ";" +
                        "-fx-text-fill: " + ("QRIS".equals(metode) ? "#B45309" : "#4F46E5") + ";" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 3 14 3 14;" +
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
        Label lblTotal = new Label(formatCurrency(t.getTotal()));
        lblTotal.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #16A34A;");
        rightInfo.getChildren().addAll(lblTotalLabel, lblTotal);

        headerRow.getChildren().addAll(leftInfo, spacer, rightInfo);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #E5E7EB;");

        Label lblItems = new Label("Item Pesanan:");
        lblItems.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #374151;");

        FlowPane itemsGrid = new FlowPane();
        itemsGrid.setHgap(12);
        itemsGrid.setVgap(12);

        for (TransaksiDetail td : details) {
            HBox itemCard = new HBox(16);
            itemCard.setPadding(new Insets(12, 16, 12, 16));
            itemCard.setAlignment(Pos.CENTER_LEFT);
            itemCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-color: #E5E7EB;" +
                            "-fx-border-radius: 8;" +
                            "-fx-border-width: 1;" +
                            "-fx-pref-width: 260;"
            );

            VBox itemInfo = new VBox(2);
            itemInfo.setPrefWidth(160);
            Label lblNama = new Label(td.getNamaMenu());
            lblNama.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #111827;");
            Label lblQty = new Label("Qty: " + td.getQty());
            lblQty.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");
            itemInfo.getChildren().addAll(lblNama, lblQty);

            Region itemSpacer = new Region();
            HBox.setHgrow(itemSpacer, Priority.ALWAYS);

            Label lblSubtotal = new Label(formatCurrency(td.getSubtotal()));
            lblSubtotal.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #374151;");

            itemCard.getChildren().addAll(itemInfo, itemSpacer, lblSubtotal);
            itemsGrid.getChildren().add(itemCard);
        }

        card.getChildren().addAll(headerRow, sep, lblItems, itemsGrid);
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
        root.setStyle("-fx-background-color: white; -fx-background-radius: 18;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Transaksi Baru");
        lblTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button btnClose = new Button("×");
        btnClose.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #4B5563;" +
                        "-fx-font-size: 22;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0;"
        );
        btnClose.setOnAction(e -> dialog.close());

        header.getChildren().addAll(lblTitle, headerSpacer, btnClose);

        HBox inputRow = new HBox(18);
        inputRow.setAlignment(Pos.BOTTOM_LEFT);

        VBox menuBox = new VBox(8);
        HBox.setHgrow(menuBox, Priority.ALWAYS);
        Label lblPilihMenu = fieldLabel("Pilih Menu");

        ComboBox<Menu> cmbMenu = new ComboBox<>();
        cmbMenu.setItems(FXCollections.observableArrayList(menuDAO.findAll()));
        cmbMenu.setPromptText("Pilih menu");
        cmbMenu.setMaxWidth(Double.MAX_VALUE);
        cmbMenu.setStyle(selectStyle());
        cmbMenu.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaMenu());
            }
        });
        cmbMenu.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Menu item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNamaMenu());
            }
        });
        menuBox.getChildren().addAll(lblPilihMenu, cmbMenu);

        VBox qtyBox = new VBox(8);
        qtyBox.setPrefWidth(130);
        Label lblJumlah = fieldLabel("Jumlah");
        TextField txtQty = new TextField("1");
        txtQty.setStyle(inputStyle());
        qtyBox.getChildren().addAll(lblJumlah, txtQty);

        Button btnTambah = new Button("Tambah");
        btnTambah.setStyle(primaryActionStyle());
        btnTambah.setPrefWidth(130);
        btnTambah.setPrefHeight(44);

        inputRow.getChildren().addAll(menuBox, qtyBox, btnTambah);

        VBox keranjangSection = new VBox(10);
        Label lblKeranjang = fieldLabel("Keranjang");

        VBox keranjangContainer = new VBox(10);
        keranjangContainer.setFillWidth(true);

        ScrollPane scrollKeranjang = new ScrollPane(keranjangContainer);
        scrollKeranjang.setFitToWidth(true);
        scrollKeranjang.setPrefHeight(150);
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
        cmbMetode.setStyle(selectStyle());
        metodeBox.getChildren().addAll(lblMetode, cmbMetode);

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        VBox totalBox = new VBox(4);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblTotalTitle = new Label("Total Pembayaran");
        lblTotalTitle.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        Label lblTotal = new Label("Rp 0");
        lblTotal.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #16A34A;");
        totalBox.getChildren().addAll(lblTotalTitle, lblTotal);

        bottomRow.getChildren().addAll(metodeBox, bottomSpacer, totalBox);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnBatal = new Button("Batal");
        btnBatal.setStyle(secondaryButtonStyle());
        btnBatal.setOnAction(e -> dialog.close());

        Button btnSimpan = new Button("Simpan Transaksi");
        btnSimpan.setStyle(enabledButtonStyle());

        footer.getChildren().addAll(btnBatal, btnSimpan);

        root.getChildren().addAll(header, inputRow, keranjangSection, bottomRow, footer);

        btnSimpan.setDisable(true);
        btnSimpan.setStyle(disabledButtonStyle());

        final Runnable[] refreshKeranjangRef = new Runnable[1];
        refreshKeranjangRef[0] = () -> renderKeranjang(
                keranjang,
                keranjangContainer,
                btnSimpan,
                lblTotal,
                refreshKeranjangRef
        );

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
                if (qty <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertUtil.showError("Validasi", "Jumlah harus angka positif.");
                return;
            }

            for (TransaksiDetail td : keranjang) {
                if (td.getIdMenu() == menu.getIdMenu()) {
                    td.setQty(td.getQty() + qty);
                    td.setSubtotal(td.getHarga() * td.getQty());
                    refreshKeranjangRef[0].run();
                    cmbMenu.setValue(null);
                    txtQty.setText("1");
                    return;
                }
            }

            TransaksiDetail detail = new TransaksiDetail(
                    0, menu.getIdMenu(), qty, menu.getHarga() * qty, "cash"
            );
            detail.setNamaMenu(menu.getNamaMenu());
            detail.setHarga(menu.getHarga());

            keranjang.add(detail);
            refreshKeranjangRef[0].run();
            cmbMenu.setValue(null);
            txtQty.setText("1");
        });

        btnSimpan.setOnAction(e -> {
            if (keranjang.isEmpty()) {
                AlertUtil.showError("Error", "Keranjang kosong!");
                return;
            }

            String metodeDb = "QRIS".equals(cmbMetode.getValue()) ? "qris" : "cash";
            double total = kasirService.hitungTotal(new ArrayList<>(keranjang));

            Transaksi transaksi = new Transaksi();
            transaksi.setIdUser(AuthService.getLoggedInUser().getIdUser());
            transaksi.setTotal(total);

            boolean success = kasirService.simpanTransaksi(
                    transaksi, new ArrayList<>(keranjang), metodeDb
            );

            if (success) {
                AlertUtil.showInfo("Berhasil", "Transaksi berhasil!\nTotal: " + formatCurrency(total));
                dialog.close();
                loadRiwayat();
            } else {
                AlertUtil.showError("Error", "Gagal menyimpan transaksi!");
            }
        });

        refreshKeranjangRef[0].run();

        Scene scene = new Scene(root, 640, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void renderKeranjang(ObservableList<TransaksiDetail> keranjang,
                                 VBox keranjangContainer,
                                 Button btnSimpan,
                                 Label lblTotal,
                                 Runnable[] refreshKeranjangRef) {
        keranjangContainer.getChildren().clear();

        if (keranjang.isEmpty()) {
            VBox emptyBox = new VBox();
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPrefHeight(86);
            emptyBox.setStyle(
                    "-fx-background-color: #FFFFFF;" +
                            "-fx-border-color: #E2E8F0;" +
                            "-fx-border-radius: 12;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-style: dashed;"
            );

            Label emptyLabel = new Label("Keranjang kosong");
            emptyLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #6B7280;");
            emptyBox.getChildren().add(emptyLabel);
            keranjangContainer.getChildren().add(emptyBox);

            btnSimpan.setDisable(true);
            btnSimpan.setStyle(disabledButtonStyle());
        } else {
            for (TransaksiDetail item : keranjang) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12, 14, 12, 14));
                row.setStyle(
                        "-fx-background-color: #F8FAFC;" +
                                "-fx-background-radius: 12;" +
                                "-fx-border-color: #E2E8F0;" +
                                "-fx-border-radius: 12;"
                );

                VBox info = new VBox(4);
                HBox.setHgrow(info, Priority.ALWAYS);

                Label nama = new Label(item.getNamaMenu());
                nama.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #111827;");

                Label detail = new Label("Qty " + item.getQty() + " x " + formatCurrency(item.getHarga()));
                detail.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");
                info.getChildren().addAll(nama, detail);

                Label subtotal = new Label(formatCurrency(item.getSubtotal()));
                subtotal.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #111827;");

                Button btnHapus = new Button("Hapus");
                btnHapus.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #EF4444;" +
                                "-fx-font-size: 12;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                );
                btnHapus.setOnAction(e -> {
                    keranjang.remove(item);
                    refreshKeranjangRef[0].run();
                });

                row.getChildren().addAll(info, subtotal, btnHapus);
                keranjangContainer.getChildren().add(row);
            }

            btnSimpan.setDisable(false);
            btnSimpan.setStyle(enabledButtonStyle());
        }

        lblTotal.setText(formatCurrency(
                keranjang.stream().mapToDouble(TransaksiDetail::getSubtotal).sum()
        ));
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 13;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );
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
        return "-fx-background-color: #0F172A;" +
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

    private String formatMetodeLabel(String metode) {
        if (metode == null) return "-";
        return switch (metode.toLowerCase()) {
            case "cash" -> "Tunai";
            case "qris" -> "QRIS";
            default -> metode;
        };
    }
}
