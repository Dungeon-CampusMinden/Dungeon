# VM Instruction Set Documentation

This documentation walks through the VM language from fundamentals to detailed instruction semantics. Use the prev/next links at the bottom of each page to move linearly, or jump via the index below.

## Page Index
- [Introduction](introduction.md)
- [Language Reference](language-reference.md)

## Conventions
- Code samples use fenced code blocks with language hints where useful.
- Instruction names are uppercase; operands appear comma-separated.
- Source positions are noted as `file:line:column`.
- Breakpoints are managed via a side table keyed by source position.
- VM executes via a generator (coroutine) so the host controls pacing; global state changes are emitted as events.

---
Prev: – | Next: [Language Reference](language-reference.md)

