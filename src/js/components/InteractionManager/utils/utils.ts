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

const posOfPointer = (e) => ({ x: e.clientX + 10, y: e.clientY + 10 });
const posOfTargetBottomEnd = (e) => {
  const rect = e.target.getBoundingClientRect();
  return { x: rect.left + rect.width, y: rect.top + rect.height };
};
const posOfTargetBottomStart = (e) => {
  const rect = e.target.getBoundingClientRect();
  return { x: rect.left, y: rect.top + rect.height };
};

export const showPreview = (e, lastE, setLastE, setPreviewPos, setPreview) => {
  // if the hovered event looks the same as the last event, return
  if (e.target && (e?.target === lastE?.target)) return;

  const previewedPage = attrIf(e, targetIsPageLink, 'data-page-title');
  if (previewedPage) {
    setPreview(previewedPage, 'page');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedHashtag = attrIf(e, targetIsHashtag, 'data-page-title');
  if (previewedHashtag) {
    setPreview(previewedHashtag, 'page');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedBlockRef = attrIf(e, targetIsBlockRef, 'data-uid');
  if (previewedBlockRef) {
    setPreview(previewedBlockRef, 'block');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedUrl = attrIf(e, targetIsUrlLink, 'href');
  if (previewedUrl) {
    setPreview(previewedUrl, 'url');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedAutoUrl = attrIf(e, targetIsAutolink, 'href');
  if (previewedAutoUrl) {
    setPreview(previewedAutoUrl, 'url');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  setLastE(e);
  setPreview(null, null);
}

export const showActions = (e, lastE, setLastE, setActionsPos, setActions, isUsingActions) => {
  // if the hovered event looks the same as the last event, return
  if (e.target && (e?.target === lastE?.target)) return;

  // get the hovered block
  const closestBlock = getClosestBlock(e);

  // if there's no block, return
  if (!closestBlock) {
    setActionsPos(null);
    setActions(null);
    return;
  };

  // Otherwise, we have a block
  if (closestBlock) {

    let targetPos = closestBlock?.getBoundingClientRect();
    setActionsPos({ x: targetPos.left + targetPos.width, y: targetPos.top });
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