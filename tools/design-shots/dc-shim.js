/*
 * Minimal stand-in for the design canvas runtime ("support.js", which is not in the repo).
 * It implements only what design/project/*.dc.html uses: <x-dc>, <helmet>, <sc-for>, <sc-if>,
 * {{path}} interpolation, onClick handlers and a DCLogic component with state + renderVals().
 * Used by render.sh to render the artboards headless; never shipped.
 */
(function () {
  class DCLogic {
    constructor(props) { this.props = props || {}; this.state = {}; }
    setState(patch) { Object.assign(this.state, patch); window.__dcRender(); }
  }
  window.DCLogic = DCLogic;

  const FONT_DIR = window.__FONT_DIR__ || '';
  const FONTS = [
    ['Anton', 'anton.ttf', '400'], ['Bebas Neue', 'bebas_neue.ttf', '400'],
    ['Archivo Black', 'archivo_black.ttf', '400'], ['Instrument Serif', 'instrument_serif.ttf', '400'],
    ['Space Grotesk', 'space_grotesk.ttf', '300 700'], ['Geist', 'geist.ttf', '100 900'],
    ['Geist Mono', 'geist_mono.ttf', '100 900'], ['Limelight', 'limelight.ttf', '400'],
    ['Orbitron', 'orbitron.ttf', '400 900'], ['Press Start 2P', 'press_start_2p.ttf', '400'],
    ['Righteous', 'righteous.ttf', '400'], ['Caveat', 'caveat.ttf', '400 700'], ['VT323', 'vt323.ttf', '400'],
    ['Special Elite', 'special_elite.ttf', '400'], ['Fredoka', 'fredoka.ttf', '300 700'],
    ['Share Tech Mono', 'share_tech_mono.ttf', '400'], ['DM Mono', 'dm_mono.ttf', '400'], ['DM Mono', 'dm_mono_medium.ttf', '500'],
    ['Chakra Petch', 'chakra_petch_medium.ttf', '500'], ['Chakra Petch', 'chakra_petch_semibold.ttf', '600'],
    ['Chakra Petch', 'chakra_petch_bold.ttf', '700'],
  ];
  const BUNDLED = FONTS.map((f) => f[0]);

  function resolve(path, scope) {
    return path.trim().split('.').reduce((v, k) => (v == null ? v : v[k]), scope);
  }

  function interpolate(str, scope) {
    return str.replace(/\{\{\s*([^}]+?)\s*\}\}/g, (_, p) => {
      if (p === 'true' || p === 'false') return p;
      const v = resolve(p, scope);
      return v == null ? '' : String(v);
    });
  }

  function expand(node, scope, out) {
    if (node.nodeType === 3) { out.appendChild(document.createTextNode(interpolate(node.nodeValue, scope))); return; }
    if (node.nodeType !== 1) return;
    const tag = node.tagName.toLowerCase();
    if (tag === 'sc-for') {
      const list = resolve(node.getAttribute('list').replace(/[{}]/g, ''), scope) || [];
      const as = node.getAttribute('as');
      list.forEach((item) => {
        const inner = Object.assign({}, scope, { [as]: item });
        node.childNodes.forEach((c) => expand(c, inner, out));
      });
      return;
    }
    if (tag === 'sc-if') {
      const raw = node.getAttribute('value').replace(/[{}]/g, '').trim();
      const val = raw === 'true' ? true : raw === 'false' ? false : resolve(raw, scope);
      if (val) node.childNodes.forEach((c) => expand(c, scope, out));
      return;
    }
    const el = document.createElement(tag);
    for (const attr of node.attributes) {
      const m = /^\{\{\s*([^}]+?)\s*\}\}$/.exec(attr.value);
      if (m && typeof resolve(m[1], scope) === 'function') {
        const fn = resolve(m[1], scope);
        el.addEventListener(attr.name.replace(/^on/, '').toLowerCase(), fn);
        continue;
      }
      el.setAttribute(attr.name, interpolate(attr.value, scope));
    }
    node.childNodes.forEach((c) => expand(c, scope, el));
    out.appendChild(el);
  }

  function freezeAnimations() {
    document.getAnimations().forEach((a) => {
      const t = a.effect.getComputedTiming();
      a.pause();
      // Entrances end where they finish; endless loops are held at a stable early frame.
      a.currentTime = Number.isFinite(t.endTime) ? t.endTime : (t.delay || 0) + 1500;
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    const scriptEl = document.querySelector('script[data-dc-script]');
    const root = document.querySelector('x-dc');
    const helmet = root.querySelector('helmet');
    const template = document.createElement('div');
    Array.from(root.childNodes).forEach((n) => { if (n !== helmet) template.appendChild(n); });
    if (helmet) {
      Array.from(helmet.children).forEach((c) => {
        if (c.tagName === 'LINK' && /fonts\.googleapis/.test(c.href || '')) {
          const wanted = (c.href.match(/family=([^&:]+)/g) || []).map((s) => decodeURIComponent(s.slice(7)).replace(/\+/g, ' '));
          const missing = wanted.filter((f) => !BUNDLED.includes(f));
          if (missing.length) {
            const l = document.createElement('link');
            l.rel = 'stylesheet';
            l.href = 'https://fonts.googleapis.com/css2?' + missing.map((f) => 'family=' + f.replace(/ /g, '+')).join('&') + '&display=swap';
            document.head.appendChild(l);
          }
        } else if (c.tagName !== 'LINK' || c.rel !== 'preconnect') {
          document.head.appendChild(c);
        }
      });
    }
    const style = document.createElement('style');
    style.textContent = FONTS.map(([n, f, w]) => `@font-face{font-family:'${n}';src:url('${FONT_DIR}/${f}');font-weight:${w};}`).join('\n');
    document.head.appendChild(style);

    const defaults = {};
    try {
      const props = JSON.parse(scriptEl.getAttribute('data-props') || '{}');
      Object.keys(props).forEach((k) => { if (props[k] && 'default' in props[k]) defaults[k] = props[k].default; });
    } catch (e) { /* no props */ }
    const Component = new Function('DCLogic', scriptEl.textContent + '; return Component;')(DCLogic);
    const logic = new Component(Object.assign(defaults, window.__PROPS__ || {}));

    window.__dcRender = () => {
      const vals = logic.renderVals();
      root.replaceChildren();
      template.childNodes.forEach((c) => expand(c, vals, root));
      document.fonts.ready.then(() => requestAnimationFrame(freezeAnimations));
    };
    window.__dcRender();
  });
})();
