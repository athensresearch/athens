export const getClosestBlock = (e) => e?.target?.closest('.block-container');
export const targetIsPageLink = (e) => e?.target?.classList?.contains('link');
export const targetIsUrlLink = (e) => e?.target?.classList?.contains('url-link');
export const targetIsHashtag = (e) => e?.target?.classList?.contains('hashtag');
export const targetIsAutolink = (e) => e?.target?.classList?.contains('autolink');
export const targetIsBlockRef = (e) => e?.target?.classList?.contains('block-ref');

export const attrIf = (e, condition, attr) => {
  if ((e.target) && condition(e)) return e.target.getAttribute(attr);
  return false;
};

export const showPreview = (e, setPreviewPos, setPreview) => {
  setPreviewPos({ x: e.clientX, y: e.clientY });

  const previewedPage = attrIf(e, targetIsPageLink, 'data-page-title');
  if (previewedPage) {
    setPreview(previewedPage, 'page');
    return;
  }

  const previewedHashtag = attrIf(e, targetIsHashtag, 'data-page-title');
  if (previewedHashtag) {
    setPreview(previewedHashtag, 'page');
    return;
  }

  const previewedBlockRef = attrIf(e, targetIsBlockRef, 'data-uid');
  if (previewedBlockRef) {
    setPreview(previewedBlockRef, 'block');
    return;
  }

  const previewedUrl = attrIf(e, targetIsUrlLink, 'href');
  if (previewedUrl) {
    setPreview(previewedUrl, 'url');
    return;
  }

  const previewedAutoUrl = attrIf(e, targetIsAutolink, 'href');
  if (previewedAutoUrl) {
    setPreview(previewedAutoUrl, 'url');
    return;
  }

  setPreview(null, null);
}

export const showActions = (e, lastE, setLastE, setActionsPos, setActions) => {
  // get the hovered block
  const closestBlock = getClosestBlock(e);

  // if there's no block, return
  if (!closestBlock) return;

  // if the hovered event looks the same as the last event, return
  if (e.target && (e?.target === lastE?.target)) return;

  // Otherwise, we have a block
  if (closestBlock) {
    console.log('hovered', closestBlock)

    //
    let targetPos = closestBlock?.getBoundingClientRect();
    setActionsPos({ x: targetPos.left + targetPos.width, y: targetPos.top });
    // uid = closestBlock.getAttribute('data-uid');
    // childrenUids = closestBlock.getAttribute('data-children-uids');
    setActions([
      { children: "test 1" },
      { children: "test 2" },
      { children: "test 3", isExtra: true },
      { children: "test 4" }
    ]);
  };
  setLastE(e);
}

export const setBlockHovered = (e) => {
  const targetBlock = getClosestBlock(e);
  if (targetBlock) {
    e.stopPropagation();
    targetBlock.classList.add('is-hovered');
    targetBlock.addEventListener('mouseout', () => targetBlock.classList.remove('is-hovered'));
  }
}