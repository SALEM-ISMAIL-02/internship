---
name: Safyron Industrial Safety Design System
colors:
  surface: '#051424'
  surface-dim: '#051424'
  surface-bright: '#2c3a4c'
  surface-container-lowest: '#010f1f'
  surface-container-low: '#0d1c2d'
  surface-container: '#122131'
  surface-container-high: '#1c2b3c'
  surface-container-highest: '#273647'
  on-surface: '#d4e4fa'
  on-surface-variant: '#c4c6cd'
  inverse-surface: '#d4e4fa'
  inverse-on-surface: '#233143'
  outline: '#8e9197'
  outline-variant: '#44474d'
  surface-tint: '#b5c8e4'
  primary: '#b5c8e4'
  on-primary: '#1f3247'
  primary-container: '#0f2338'
  on-primary-container: '#788ba4'
  inverse-primary: '#4d6078'
  secondary: '#ffb599'
  on-secondary: '#5a1c00'
  secondary-container: '#f66018'
  on-secondary-container: '#4f1700'
  tertiary: '#4edea3'
  on-tertiary: '#003824'
  tertiary-container: '#002819'
  on-tertiary-container: '#009d6c'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#d2e4ff'
  primary-fixed-dim: '#b5c8e4'
  on-primary-fixed: '#071d31'
  on-primary-fixed-variant: '#36485f'
  secondary-fixed: '#ffdbce'
  secondary-fixed-dim: '#ffb599'
  on-secondary-fixed: '#370e00'
  on-secondary-fixed-variant: '#7f2b00'
  tertiary-fixed: '#6ffbbe'
  tertiary-fixed-dim: '#4edea3'
  on-tertiary-fixed: '#002113'
  on-tertiary-fixed-variant: '#005236'
  background: '#051424'
  on-background: '#d4e4fa'
  surface-variant: '#273647'
typography:
  headline-xl:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  headline-xl-mobile:
    fontFamily: Inter
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.015em
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.015em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: JetBrains Mono
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.06em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 1rem
  margin: 2rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2.5rem
---

## Brand & Style

This design system establishes a high-precision, technical aesthetic engineered for industrial safety compliance, certification tracking, and workforce training. It merges technical rigor with modern digital utility, evoking the uncompromising reliability of heavy industrial engineering, avionics telemetry, and mission-critical control rooms.

The emotional baseline is authoritative, calm, and exacting. For learners operating in hazardous environments, the interface provides clarity, reducing cognitive load during time-sensitive compliance assessments. For Senior Safety Directors and Compliance Officers, it delivers dense, scannable telemetry with absolute data fidelity.

The visual direction combines **Technical Precision** with **Tactile Industrial Structure**:
- Subtle dark metallic substrates paired with crisp grid dividers.
- High-visibility safety indicators borrowed from standard ISO/OSHA physical warning palettes.
- Monospaced numeric data callouts for instrument readings, certification tracking codes, and regulatory timestamps.
- Zero decorative fluff: every micro-interaction, container border, and accent color reinforces status hierarchy, risk mitigation, and audit readiness.

## Colors

The palette is rooted in industrial control and safety protocols, utilizing a dark-first environment that minimizes eye fatigue during extended desk audits while preserving high contrast for critical alerts.

### Palette Architecture
- **Primary (`#0F2338`)**: Deep Industrial Navy. Forms the dominant structural bedrock, navigation rails, and primary action anchor points. Evokes institutional authority, marine engineering, and structural stability.
- **Secondary (`#EA580C`)**: High-Visibility Hazard Orange. Reserved strictly for mandatory compliance alerts, urgent recertification warnings, critical hazard points, and primary attention draws.
- **Tertiary (`#10B981`)**: Precision Emerald Green. Denotes verified compliance, passed field inspections, OSHA compliance validation, and finished modules.
- **Neutral (`#94A3B8`)**: Anodized Slate. Applied across secondary labels, metadata, inactive grid lines, and structural dividers to prevent visual vibration.

### Semantic & Hazard Extensions
- **Hazard Caution Yellow (`#F59E0B`)**: Intermediate warnings, modules nearing expiration, and pending field sign-offs.
- **Surface Foundations**:
  - Base Ground: `#070F18` (deep sub-chassis)
  - Surface Tier 1 (Canvas): `#0B1726`
  - Surface Tier 2 (Cards & Modules): `#0F2338`
  - Surface Tier 3 (Raised Panels & Modals): `#17304B`
- **Text & Telemetry**:
  - Primary Text: `#F8FAFC` (High-contrast structural white)
  - Muted Technical Text: `#94A3B8`
  - Ghost Borders: `rgba(148, 163, 184, 0.16)`

## Typography

The type system pairs **Inter** for high-density structural comprehension with **JetBrains Mono** for all regulatory metrics, module indexes, safety classification levels, and timestamp data.

### Typographic Hierarchy
- **Headlines (Inter)**: Weighted between Medium (500) and Bold (700) with slight negative tracking on display sizes to create cohesive, solid headings akin to industrial hardware plaques.
- **Body Text (Inter)**: Optimized for crisp reading in low-light environments. Generous line-heights (1.5x–1.6x) prevent textual fatigue across long safety regulations and SOP procedures.
- **Labels & Metrics (JetBrains Mono)**: Applied to certification numbers (e.g., `ISO-45001-REV3`), progress percentages, step counts (`STEP 04/12`), status chips, and machine telemetry. All numbers must utilize tabular figures (`tnum`) for flawless column alignment.

## Layout & Spacing

Layouts adhere to an engineering-grade 12-column fluid grid on desktop and 4 columns on mobile devices, maintaining structured visual vertical columns reminiscent of technical blueprints and system schematics.

### Grid & Breakpoints
- **Desktop (≥ 1280px)**: 12 columns, `margin: 2rem`, `gutter: 1.5rem`. Enables multi-column dashboard views combining video lecture feeds, technical blueprints, and live interactive checklists side by side.
- **Tablet (768px – 1279px)**: 8 columns, `margin: 1.5rem`, `gutter: 1rem`. Side navigation collapses to an icon-rail with telemetry flyouts.
- **Mobile (< 768px)**: 4 columns, `margin: 1rem`, `gutter: 1rem`. Full stacked layout; metrics and progress bars prioritize vertical flow to allow one-handed field inspection logging.

### Layout Principles
- Spacing strictly follows an 8pt base grid with a 4pt micro-increment (`space-xs: 0.25rem`).
- Information density is balanced: high data-density for tabular modules and engineering readouts; generous, controlled spacing (`space-lg`, `space-xl`) around critical safety warnings and test confirmation actions to prevent accidental taps.

## Elevation & Depth

Visual hierarchy is communicated through structural layering, cold mechanical tonal surfaces, and crisp edge highlights rather than soft, ambient consumer shadows.

### Depth Hierarchy
1. **Sub-Chassis (Level 0)**: Background `#070F18`. The unlit, recessed layer for screen frames and workspace canvas.
2. **Structural Tier 1 (Level 1)**: Base cards and modules `#0F2338` edged with a 1px border of `rgba(148, 163, 184, 0.12)`.
3. **Elevated Instrumentation (Level 2)**: Active modules, focused step-cards, and floating control trays `#17304B` supported by a sharp industrial drop shadow:
   `box-shadow: 0 4px 12px rgba(2, 6, 23, 0.65), 0 1px 2px rgba(0, 0, 0, 0.40)`.
4. **Modal & Critical Hazard Overlays (Level 3)**: `#1E3A5F` bordered with high-visibility accent strokes (e.g., `#EA580C` for critical stop-work notices or hazard alerts) accompanied by `box-shadow: 0 12px 32px rgba(0, 0, 0, 0.75), 0 0 0 1px rgba(234, 88, 12, 0.35)`.

Subtle inner top-edge highlights (`inset 0 1px 0 rgba(255, 255, 255, 0.08)`) are applied to cards and interactive panels to replicate the beveled metallic finish of ruggedized field tablets.

## Shapes

The design uses a restrained, industrial radius (Level 1 / Soft). Roundedness is kept tight (`4px` base, `8px` for large containers, `0px` for technical indicators and meter dividers) to reflect precision machining and structural metalwork.

- **Buttons, Inputs & Small Chips**: `rounded` (`4px` / `0.25rem`). Ensures crisp visual bounding boxes that align cleanly with neighboring grid lines.
- **Cards, Modals & Inspection Panes**: `rounded-lg` (`8px` / `0.5rem`). Softens the container perimeter while maintaining a compact, professional profile.
- **Status Indicators, Code Badges & Hazard Tabs**: `0px` border-radius or chamfered right-edge markers to evoke physical industrial warning tags and safety lockout labels.

## Components

### Buttons & Interactive Controls
- **Primary Action (Certification / Submit)**: Solid `#EA580C` (Caution Orange) background, `#FFFFFF` text, `4px` radius. Monospaced label (`JetBrains Mono`, uppercase, letter-spacing `0.04em`). Subtle inset top glow. On hover, brightness escalates to `#F97316` with a crisp outer focus ring (`2px` solid `#FB923C`, `2px` offset).
- **Secondary / Technical Action**: Deep Navy surface `#17304B` with a `1px` border of `rgba(148, 163, 184, 0.24)` and `#F8FAFC` text.
- **Danger / Stop-Work Command**: Deep crimson `#DC2626` base with an alternating diagonal hazard stripe overlay on active/focus state.

### Status Badges & Chips
- Designed as standardized industrial tags. Compact padding (`2px 8px`), square or `2px` micro-radius, JetBrains Mono font (`label-sm`).
- **Compliant / Verified**: `#064E3B` surface, `#10B981` border, `#34D399` text, prefixed with a solid emerald verification glyph (`[✓]`).
- **Hazard / Action Required**: `#7C2D12` surface, `#EA580C` border, `#FFEDD5` text.
- **Under Review / Caution**: `#78350F` surface, `#F59E0B` border, `#FEF3C7` text.

### Industrial Progress Bars
- High-tactility segmented gauges rather than generic smooth pill bars.
- Track surface: `#070F18` recessed channel with a 1px inner shadow.
- Completed Fill: `#10B981` or `#EA580C` partitioned into distinct, readable block segments (e.g., 5% increments) using 1.5px divider lines to evoke LED ladder indicators on industrial control racks.

### Cards & Module Containers
- Solid `#0F2338` backing with a precise 1px stroke of `rgba(148, 163, 184, 0.16)`.
- Cards feature a dedicated header strip holding the module index number (`MODULE-04 // ELECTRICAL LOCKOUT`) in JetBrains Mono muted text (`#94A3B8`), separated by a hairline divider from the card content.

### Inputs & Checkboxes
- **Input Fields**: Recessed `#070F18` background, `1px` border of `rgba(148, 163, 184, 0.2)`. Active typing displays high-contrast white text `#F8FAFC` with a sharp `#EA580C` accent border.
- **Checkboxes & Radios**: Chamfered square forms (`2px` radius). Active state fills with `#10B981` displaying a sharp, heavy geometric tick mark designed for visibility through smudged glass or field gloves.

### Specialized Safety Components
- **OSHA Compliance Lockout Banner**: A full-bleed notification with left-aligned caution chevron patterns, pinning mandatory regulatory notes to the top of examination screens.
- **Telemetry Readout Matrix**: Dense tabular containers presenting safety metrics (e.g., Lost Time Injury Frequency Rate, Days Without Incident, Certification Expiry Countdown) in monospaced tabular figures with immediate chromatic indicator dots.