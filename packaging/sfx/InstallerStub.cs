using System;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Runtime.InteropServices;
using System.Windows.Forms;

namespace HanyarNgopiInstaller
{
    internal static class InstallerStub
    {
        private static readonly byte[] Marker = System.Text.Encoding.ASCII.GetBytes("__HANYAR_NGOPI_PAYLOAD_V1__");

        [STAThread]
        private static int Main()
        {
            try
            {
                string exePath = Application.ExecutablePath;
                byte[] self = File.ReadAllBytes(exePath);
                int markerIndex = FindLast(self, Marker);

                if (markerIndex < 0)
                {
                    throw new InvalidOperationException("Payload aplikasi tidak ditemukan.");
                }

                int payloadStart = markerIndex + Marker.Length;
                string tempZip = Path.Combine(Path.GetTempPath(), "HanyarNgopi-portable.zip");
                string appDir = Path.Combine(
                    Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
                    "Programs",
                    "Manajemen HanyarNgopi");

                if (Directory.Exists(appDir))
                {
                    Directory.Delete(appDir, true);
                }

                Directory.CreateDirectory(appDir);

                using (FileStream zip = File.Create(tempZip))
                {
                    zip.Write(self, payloadStart, self.Length - payloadStart);
                }

                ZipFile.ExtractToDirectory(tempZip, appDir);
                TryCreateDesktopShortcut(appDir);

                string launcher = Path.Combine(appDir, "HanyarNgopi.exe");
                Process.Start(new ProcessStartInfo
                {
                    FileName = launcher,
                    WorkingDirectory = appDir,
                    UseShellExecute = true
                });

                return 0;
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    ex.Message,
                    "Installer Manajemen HanyarNgopi",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error);
                return 1;
            }
        }

        private static int FindLast(byte[] source, byte[] pattern)
        {
            for (int i = source.Length - pattern.Length; i >= 0; i--)
            {
                bool matched = true;
                for (int j = 0; j < pattern.Length; j++)
                {
                    if (source[i + j] != pattern[j])
                    {
                        matched = false;
                        break;
                    }
                }

                if (matched)
                {
                    return i;
                }
            }

            return -1;
        }

        private static void TryCreateDesktopShortcut(string appDir)
        {
            try
            {
                Type shellType = Type.GetTypeFromProgID("WScript.Shell");
                if (shellType == null)
                {
                    return;
                }

                dynamic shell = Activator.CreateInstance(shellType);
                string shortcutPath = Path.Combine(
                    Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory),
                    "Manajemen HanyarNgopi.lnk");

                dynamic shortcut = shell.CreateShortcut(shortcutPath);
                shortcut.TargetPath = Path.Combine(appDir, "HanyarNgopi.exe");
                shortcut.WorkingDirectory = appDir;
                shortcut.IconLocation = Path.Combine(appDir, "HanyarNgopi.exe") + ",0";
                shortcut.Save();

                Marshal.FinalReleaseComObject(shortcut);
                Marshal.FinalReleaseComObject(shell);
            }
            catch
            {
                // Shortcut creation is helpful, but the app should still install without it.
            }
        }
    }
}
