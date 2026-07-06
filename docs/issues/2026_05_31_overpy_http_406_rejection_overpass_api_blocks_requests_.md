# overpy HTTP 406 rejection — Overpass API blocks requests with no User-Agent

**Severity:** sev2  
**Date:** 2026-05-31

## Root cause

overpy's API.query() uses urllib.urlopen() with no User-Agent header. overpass-api.de rejects requests without a User-Agent with HTTP 406 Not Acceptable.

## Resolution

Dropped overpy entirely. All queries use requests.post() with User-Agent: track-poly-poc/0.1.0. Raw JSON response parsed directly.
