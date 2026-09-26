# Grade 12 Textbooks data

Replace all `REPLACE_WITH_...` values before publishing:

1. Use only rights-verified PDF URLs.
2. Calculate each SHA-256 checksum from the exact downloaded PDF bytes.
3. Store lowercase 64-character hexadecimal checksums.
4. Keep every book ID globally unique.
5. Preserve both `stream` and `subjectId`; mathematics can exist in both streams.
6. Increase `version` whenever the catalog changes.
7. Update `version.json` after attaching the matching APK GitHub Release.

Do not commit PDFs, credentials, tokens, or private keys.
