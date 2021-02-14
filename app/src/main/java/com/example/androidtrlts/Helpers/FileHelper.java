package com.example.androidtrlts.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import com.example.androidtrlts.Activities.TextEditorActivity;
import com.example.androidtrlts.Adapters.ItemAdapter;
import com.example.androidtrlts.Model.Item;
import com.example.androidtrlts.R;
import com.example.androidtrlts.Utils.FileList;
import com.example.androidtrlts.Utils.Util;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHelper {

    public static File copyFile(File src, File path) throws Exception {

        try {

            if (src.isDirectory()) {

                if (src.getPath().equals(path.getPath())) throw new Exception();

                File directory = createDirectory(path, src.getName());

                for (File file : src.listFiles()) copyFile(file, directory);

                return directory;
            }
            else {

                File file = new File(path, src.getName());

                FileChannel channel = new FileInputStream(src).getChannel();

                channel.transferTo(0, channel.size(), new FileOutputStream(file).getChannel());

                return file;
            }
        }
        catch (Exception e) {

            throw new Exception(String.format("Error copying %s", src.getName()));
        }
    }

    //----------------------------------------------------------------------------------------------

    public static File createDirectory(File path, String name) throws Exception {

        File directory = new File(path, name);

        if (directory.mkdirs()) return directory;

        if (directory.exists()) throw new Exception(String.format("%s already exists", name));

        throw new Exception(String.format("Error creating %s", name));
    }

    public static File deleteFile(File file) throws Exception {

        if (file.isDirectory()) {

            for (File child : file.listFiles()) {

                deleteFile(child);
            }
        }

        if (file.delete()) return file;

        throw new Exception(String.format("Error deleting %s", file.getName()));
    }

    public static File renameFile(File file, String name) throws Exception {

        String extension = getExtension(file.getName());

        if (!extension.isEmpty()) name += "." + extension;

        File newFile = new File(file.getParent(), name);

        if (file.renameTo(newFile)) return newFile;

        throw new Exception(String.format("Error renaming %s", file.getName()));
    }

    public static File unzip(File zip) throws Exception {

        File directory = createDirectory(zip.getParentFile(), removeExtension(zip.getName()));

        FileInputStream fileInputStream = new FileInputStream(zip);

        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        try (ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream)) {

            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {

                byte[] buffer = new byte[1024];

                File file = new File(directory, zipEntry.getName());

                if (zipEntry.isDirectory()) {

                    if (!file.mkdirs()) throw new Exception("Error uncompressing");
                }
                else {

                    int count;

                    try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                        while ((count = zipInputStream.read(buffer)) != -1) {

                            fileOutputStream.write(buffer, 0, count);
                        }
                    }
                }
            }
        }

        return directory;
    }

    public static String read(File file){
        StringBuilder text = new StringBuilder();
        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while((line = bufferedReader.readLine()) != null){
                text.append(line);
                text.append('\n');
            }
            bufferedReader.close();
            return text.toString();
        }catch (IOException e){
            return "";
        }
    }


    public static File getInternalStorage() {

        //returns the path to the internal storage

        return Environment.getExternalStorageDirectory();
    }

    //----------------------------------------------------------------------------------------------

    public static File getExternalStorage() {

        //returns the path to the external storage or null if it doesn't exist

        String path = System.getenv("SECONDARY_STORAGE");

        return path != null ? new File(path) : null;
    }

    public static File getPublicDirectory(String type) {

        //returns the path to the public directory of the given type

        return Environment.getExternalStoragePublicDirectory(type);
    }

    public static String getAlbum(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        }
        catch (Exception e) {

            return null;
        }
    }

    //----------------------------------------------------------------------------------------------

    public static String getArtist(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        }
        catch (Exception e) {

            return null;
        }
    }

    public static String getDuration(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long milliseconds = Long.parseLong(duration);

            long s = milliseconds / 1000 % 60;

            long m = milliseconds / 1000 / 60 % 60;

            long h = milliseconds / 1000 / 60 / 60 % 24;

            if (h == 0) return String.format(Locale.getDefault(), "%02d:%02d", m, s);

            return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
        }
        catch (Exception e) {

            return null;
        }
    }

    public static String getLastModified(File file) {

        //returns the last modified date of the given file as a formatted string

        return DateFormat.format("dd MMM yyy", new Date(file.lastModified())).toString();
    }

    public static String getMimeType(File file) {

        //returns the mime type for the given file or null iff there is none

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(file.getName()));
    }

    public static String getName(File file) {

        //returns the name of the file hiding extensions of known file types

        switch (FileType.getFileType(file)) {

            case DIRECTORY:
                return file.getName();

            case MISC_FILE:
                return file.getName();

            default:
                return removeExtension(file.getName());
        }
    }

    public static String getPath(File file) {

        //returns the path of the given file or null if the file is null

        return file != null ? file.getPath() : null;
    }

    public static long getSize(File file) {

        if (file.isDirectory()) {

            File[] children = getChildren(file);

            if (children == null) return 0;

            return children.length;
        }

        return file.length();

    }

    public static String getStorageUsage(Context context) {

        File internal = getInternalStorage();

        File external = getExternalStorage();

        long f = internal.getFreeSpace();

        long t = internal.getTotalSpace();

        if (external != null) {

            f += external.getFreeSpace();

            t += external.getTotalSpace();
        }

        String use = Formatter.formatShortFileSize(context, t - f);

        String tot = Formatter.formatShortFileSize(context, t);

        return String.format("%s used of %s", use, tot);
    }

    public static String getTitle(File file) {

        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        }
        catch (Exception e) {

            return null;
        }
    }

    public static String getExtension(String filename) {

        //returns the file extension or an empty string iff there is no extension

        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "";
    }

    //----------------------------------------------------------------------------------------------

    public static String removeExtension(String filename) {

        int index = filename.lastIndexOf(".");

        return index != -1 ? filename.substring(0, index) : filename;
    }

    public static int compareDate(File file1, File file2) {

        long lastModified1 = file1.lastModified();

        long lastModified2 = file2.lastModified();

        return Long.compare(lastModified2, lastModified1);
    }

    //----------------------------------------------------------------------------------------------

    public static int compareName(File file1, File file2) {

        String name1 = file1.getName();

        String name2 = file2.getName();

        return name1.compareToIgnoreCase(name2);
    }

    public static int compareSize(File file1, File file2) {

        long length1 = file1.length();

        long length2 = file2.length();

        return Long.compare(length2, length1);
    }

    //----------------------------------------------------------------------------------------------


    public static int getImageResource(File file) {
        if(file.isDirectory()){
            return R.drawable.ic_folder_outline_24dp;
        }
        switch (FilenameUtils.getExtension(file.getAbsolutePath()).toLowerCase()) {

            case "mp4":
                return R.drawable.ic_mp4_outline_24dp;

            case "jpg":
                return R.drawable.ic_jpg_outline_24dp;

            case "jpeg":
                return R.drawable.ic_jpeg_outline_24dp;

            case "png":
                return R.drawable.ic_png_outline_24dp;

            case "doc":
                return R.drawable.ic_doc_outline_24dp;

            case "docx":
                return R.drawable.ic_docx_outline_24dp;

            case "ppt":
                return R.drawable.ic_ppt_outline_24dp;

            case "xls":
                return R.drawable.ic_xls_outline_24dp;

            case "pdf":
                return R.drawable.ic_pdf_outline_24dp;

            case "txt":
                return R.drawable.ic_txt_outline_24dp;

            case "html":
                return R.drawable.ic_html_outline_24dp;

            case "ttf":
                return R.drawable.ic_ttf_outline_24dp;

            case "zip":
                return R.drawable.ic_zip_outline_24dp;

            default:
                return R.drawable.ic_file_outline_24dp;
        }
    }

    public static boolean isStorage(File dir) {

        return dir == null || dir.equals(getInternalStorage()) || dir.equals(getExternalStorage());
    }

    //----------------------------------------------------------------------------------------------

    public static File[] getChildren(File directory) {

        if (!directory.canRead()) return null;
        return directory.listFiles(file -> file.exists() && !file.isHidden());
    }

    //----------------------------------------------------------------------------------------------

    public static ArrayList<File> getAudioLibrary(Context context) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String data[] = new String[]{MediaStore.Audio.Media.DATA};

        String selection = MediaStore.Audio.Media.IS_MUSIC;

        Cursor cursor = new CursorLoader(context, uri, data, selection, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists()) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    //----------------------------------------------------------------------------------------------

    public static ArrayList<File> getImageLibrary(Context context) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String data[] = new String[]{MediaStore.Images.Media.DATA};

        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists()) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    public static ArrayList<File> getVideoLibrary(Context context) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String data[] = new String[]{MediaStore.Video.Media.DATA};

        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists()) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    public static ArrayList<File> searchFilesName(Context context, String name) {

        ArrayList<File> list = new ArrayList<>();

        Uri uri = MediaStore.Files.getContentUri("external");

        String data[] = new String[]{MediaStore.Files.FileColumns.DATA};

        Cursor cursor = new CursorLoader(context, uri, data, null, null, null).loadInBackground();

        if (cursor != null) {

            while (cursor.moveToNext()) {

                File file = new File(cursor.getString(cursor.getColumnIndex(data[0])));

                if (file.exists() && file.getName().startsWith(name)) list.add(file);
            }

            cursor.close();
        }

        return list;
    }

    public static List<Item> getItems(String path, String filter){
        ArrayList<Item>  items = new ArrayList<>();
        File dir = new File(path); // folder
        File[] files = null;
        if(!dir.exists() || !dir.isDirectory()){
           return null;
        }

       files = getChildren(dir);

        if(files == null){
            return null;
        }

        String [] extensions = {"txt","html","xhtml"};

        for (File file: files){
            if (file.isDirectory()) {
                // directory
                long count = getSize(file);
                Item item;

                if(filter != null){
                    if(file.getName().toLowerCase().contains(filter.toLowerCase())){
                        item = new Item(file.getName(), file.lastModified(), ItemAdapter.Types.FOLDER, count);
                        item.setImageResource(getImageResource(file));
                        items.add(item);
                    }
                }else{
                    item = new Item(file.getName(), file.lastModified(), ItemAdapter.Types.FOLDER, count);
                    item.setImageResource(getImageResource(file));
                    items.add(item);
                }
            }else{
                // file
                String extension = getExtension(file.getName());
                if (Arrays.asList(extensions).contains(extension)) {
                    Item item;
                    if(filter != null){
                        if(file.getName().toLowerCase().contains(filter.toLowerCase())){
                            item = new Item(file.getName(), file.lastModified(), ItemAdapter.Types.FILE, extension);
                            item.setImageResource(getImageResource(file));
                            items.add(item);
                        }
                    }else{
                        item = new Item(file.getName(), file.lastModified(), ItemAdapter.Types.FILE, extension);
                        item.setImageResource(getImageResource(file));
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    public static File validateFileName(String path){
        String extension = "";
        String  fileName = path.substring(path.lastIndexOf("/")+1);

        if(path.lastIndexOf(".") > -1){
            extension = "." + path.substring(path.lastIndexOf(".")+1);
            fileName = path.substring(path.lastIndexOf("/")+1,
                    path.lastIndexOf("."));
        }

        String dir = path.substring(0, path.lastIndexOf("/"));
        int counter = 1;
        File file = new File(path);
        String tmpFileName;
        while(file.exists()){
            tmpFileName = fileName + " (" + (counter++) + ")";
            file = new File( dir+"/"+ tmpFileName + extension);
        }
        return file;
    }

    public static File convertFileToPDF(Activity activity, File file){

        try {
            if(!file.exists()){
                throw new Exception("File not exists");
            }
            String content = FileHelper.read(file);
            if(content.isEmpty()){
                throw new Exception("Empty file.");
            }

            String title = FileHelper.getName(file);
            String filePath =  file.toString().substring(0,file.toString().lastIndexOf("."));
            File pdf = new File(filePath+".pdf");

            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12);
            Document doc = new Document();

            PdfWriter.getInstance(doc, new FileOutputStream(pdf.getAbsoluteFile()));
            doc.open();

            if(pdf.getName().endsWith(".html") || pdf.getName().endsWith(".xhtml")){
                    HTMLWorker hw = new HTMLWorker(doc);
                    hw.parse(new StringReader(content));
            }else{
                    //document settings
                    doc.setPageSize(PageSize.A4);
                    doc.addTitle(title);
                    //doc.addCreationDate();
                    //doc.addAuthor("");
                    //doc.addCreator("");
                    Paragraph paragraph = new Paragraph(Util.html2text(content));
                    paragraph.setFont(font);
                    paragraph.setAlignment(Paragraph.ALIGN_MIDDLE);
                    doc.add(paragraph);
            }
            doc.close();
            return pdf;
        }catch (DocumentException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } catch (IOException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }catch (Exception e){
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public static File exportTextToPDF(Activity activity, String text, String title){
        try {
            if(text.isEmpty()){
                throw new Exception("Text is Empty");
            }
            File file;
            String t = title;
            String s = text;

            if(TextEditorActivity.filePath != null && !TextEditorActivity.filePath.isEmpty()){
                String path;
                path = TextEditorActivity.filePath.substring(0,TextEditorActivity.filePath.lastIndexOf("."));
                file = new File(path+".pdf");
                title = TextEditorActivity.filePath.substring(TextEditorActivity.filePath.lastIndexOf("/")+1,
                        TextEditorActivity.filePath.lastIndexOf("."));
            }else{
                file = new File(FileList.currentDirPath +"/"+title+".pdf");
            }

            if(!file.exists()){
                file.createNewFile();
            }

            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 12);
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file.getAbsoluteFile()));
            doc.open();

            if(file.getName().endsWith(".html") || file.getName().endsWith(".xhtml")){
                HTMLWorker hw = new HTMLWorker(doc);
                hw.parse(new StringReader(text));
            }else{
                //document settings
                doc.setPageSize(PageSize.A4);
                doc.addTitle(title);
                //doc.addCreationDate();
                //doc.addAuthor("");
                //doc.addCreator("");
                Paragraph paragraph = new Paragraph(Util.html2text(text));
                paragraph.setFont(font);
                paragraph.setAlignment(Paragraph.ALIGN_MIDDLE);
                doc.add(paragraph);
            }
            doc.close();

            return file;
        }catch (DocumentException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } catch (IOException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }catch (Exception e){
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    public static void shareFile(Activity activity, File file, String mimeType){
        if(file == null){
          return;
        }

        Uri uri = FileProvider.getUriForFile(activity, "com.example.androidtrlts.provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivityForResult(Intent.createChooser(intent,"share file with:"),Util.SHARE_REQUEST_CODE);
    }

    public static void shareTextToPDF(Activity activity, String text, String title){
        if(text.isEmpty()){
            Toast.makeText(activity, "Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = FileHelper.exportTextToPDF(activity, text, title);
        if(file == null){
            Toast.makeText(activity, "Unable to convert to PDF", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(activity, "com.example.androidtrlts.provider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivityForResult(Intent.createChooser(intent,"share file with:"),Util.SHARE_REQUEST_CODE);
    }

    public  static boolean saveImage(Activity activity, String path, Bitmap bitmap){

        try{
            String name = path.substring(path.lastIndexOf("/")+1, path.lastIndexOf("."));
            String dir = path.substring(0, path.lastIndexOf("/"));
            String imageSavePath = dir +"/"+name+".jpg";
            File picFile = new File(imageSavePath);
            FileOutputStream fileOutputStream = new FileOutputStream(picFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.close();
            return true;
        } catch (Exception e){
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void deleteRecursive(File item){
        if(item.isDirectory()){
            for (File subItem: item.listFiles()){
                deleteRecursive(subItem);
            }
        }

        item.delete();
    }

    public enum FileType {

        DIRECTORY, MISC_FILE, AUDIO, IMAGE, VIDEO, DOC, PPT, XLS, PDF, TXT, ZIP;

        public static FileType getFileType(File file) {

            if (file.isDirectory())
                return FileType.DIRECTORY;

            String mime = FileHelper.getMimeType(file);

            if (mime == null)
                return FileType.MISC_FILE;

            if (mime.startsWith("audio"))
                return FileType.AUDIO;

            if (mime.startsWith("image"))
                return FileType.IMAGE;

            if (mime.startsWith("video"))
                return FileType.VIDEO;

            if (mime.startsWith("application/ogg"))
                return FileType.AUDIO;

            if (mime.startsWith("application/msword"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.ms-word"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.ms-powerpoint"))
                return FileType.PPT;

            if (mime.startsWith("application/vnd.ms-excel"))
                return FileType.XLS;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml"))
                return FileType.DOC;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.presentationml"))
                return FileType.PPT;

            if (mime.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml"))
                return FileType.XLS;

            if (mime.startsWith("application/pdf"))
                return FileType.PDF;

            if (mime.startsWith("text"))
                return FileType.TXT;

            if (mime.startsWith("application/zip"))
                return FileType.ZIP;

            return FileType.MISC_FILE;
        }
    }

    public static void sort(File[] files, Util.Order order, Util.Property property){
        if(order == Util.Order.ASC){
            if(property == Util.Property.NAME){
                Arrays.sort(files, (f1, f2) -> f1.getName().compareTo(f2.getName()));
            }else{
                Arrays.sort(files, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));
            }
        }else{
            if(property == Util.Property.NAME){
                Arrays.sort(files, (f1, f2) -> f2.getName().compareTo(f1.getName()));
            }else{
                Arrays.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            }
        }

    }

}
