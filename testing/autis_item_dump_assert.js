if (loaded.length !== 1) throw new Error('dump script did not register ServerEvents.loaded')
if (typeof Utils.fileExists === 'function') throw new Error('test stub accidentally provided fileExists')
loaded[0]({ server: {} })
